/*
 * FreeCast - streaming over Internet
 *
 * This code was developped by Alban Peignier (http://people.tryphon.org/~alban/) 
 * and contributors (their names can be found in the CONTRIBUTORS file).
 *
 * Copyright (C) 2004 Alban Peignier
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.kolaka.freecast.player;

import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.pipe.Consumer;
import org.kolaka.freecast.pipe.ConsumerInputStreamFactory;
import org.kolaka.freecast.service.BaseService;
import org.kolaka.freecast.service.ControlException;

import javax.sound.sampled.*;
import java.util.Date;

/**
 * 
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier </a>
 */
public class AudioPlayer extends BaseService implements InteractivePlayer {

    private Consumer consumer;

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    private SourceDataLine line;

    private PlayRunnable runnable;

    private ConsumerInputStreamFactory consumerInputStreamFactory;

    private final AudioFormat OUTPUT_FORMAT = new AudioFormat(44100, 16, 2,
            true, false);

    public void dispose() throws ControlException {
        super.dispose();

        if (line != null) {
            line.close();
            line = null;
        }
    }

    public void init() throws ControlException {
        super.init();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                OUTPUT_FORMAT);

        if (!AudioSystem.isLineSupported(info)) {
            throw new ControlException("Can't find a compatible sound ouput");
        }

        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
        } catch (LineUnavailableException e) {
            throw new ControlException("Can't obtain a sound ouput", e);
        }
    }

    public void start() throws ControlException {
        stoppedOnError = false;
        consumerInputStreamFactory = new ConsumerInputStreamFactory(consumer);

        try {
            line.open(OUTPUT_FORMAT);
        } catch (LineUnavailableException e) {
            throw new ControlException("Can't open sound ouput", e);
        }
        line.start();

        LogFactory.getLog(getClass()).debug(
                "line " + line + " is open and started");

        runnable = new PlayRunnable();
        Thread thread = new Thread(runnable, "AudioPlayer");
        thread.start();

        super.start();
    }

    public void stop() throws ControlException {
        if (runnable != null) {
            runnable.stop();
        }

        if (line != null) {
            line.drain();
            line.stop();
        }

        if (consumerInputStreamFactory != null) {
            consumerInputStreamFactory.close();
        }

        super.stop();
    }

    private boolean stoppedOnError;

    private long streamBytesLength;

    private Date streamStartDate;

    public PlayerStatus getPlayerStatus() {
        if (isStopped() || streamStartDate == null || streamBytesLength == 0) {
            return PlayerStatus.INACTIVE;
        }

        long streamTimeLength = (long) (streamBytesLength * 1000 / (OUTPUT_FORMAT
                .getFrameRate() * OUTPUT_FORMAT.getFrameSize()));
        long playTimeLength = System.currentTimeMillis()
                - streamStartDate.getTime();

        double missingDataRate = Math.max(0,
                (double) (playTimeLength - streamTimeLength) / playTimeLength);

        return new PlayerStatus(playTimeLength, missingDataRate);
    }

    class PlayRunnable implements Runnable {

        private boolean stopped;

        public void stop() {
            stopped = true;
        }

        public void run() {
            stopped = false;
            LogFactory.getLog(getClass()).debug("started");

            streamStartDate = new Date();
            streamBytesLength = 0;

            try {
                byte buffer[] = new byte[1024];

                AudioInputStream audioInput = null;

                while (!stopped) {
                    if (audioInput == null) {
                        AudioInputStream oggAudioInputStream = AudioSystem
                                                .getAudioInputStream(consumerInputStreamFactory
                                                        .next());
                        AudioFormat oggFormat = oggAudioInputStream.getFormat();
                        LogFactory.getLog(getClass()).debug(
                        "sound stream started (format: " + oggFormat + ")");
                        
                        if (oggFormat.getChannels() == 1) {
                            AudioFormat pcmFormat = 
                                new AudioFormat(OUTPUT_FORMAT.getSampleRate(), OUTPUT_FORMAT.getSampleSizeInBits(), oggFormat.getChannels(),true, false);
                            audioInput = new StereoPCMAudioInputStream(AudioSystem
                                    .getAudioInputStream(
                                            pcmFormat,
                                            oggAudioInputStream));
                        } else {
                            audioInput = AudioSystem.getAudioInputStream(OUTPUT_FORMAT, oggAudioInputStream);
                        }
                    }

                    int read = audioInput.read(buffer, 0, buffer.length);
                    if (read == -1) {
                        LogFactory.getLog(getClass()).debug(
                                "sound stream is ended");
                        audioInput = null;
                        continue;
                    }

                    streamBytesLength += read;

                    if (line != null) {
                        line.write(buffer, 0, read);
                    }
                }
            } catch (Exception e) {
                LogFactory.getLog(getClass()).error("Sound stream error", e);
                stoppedOnError = true;
                stopQuietly();
            }

            LogFactory.getLog(getClass()).debug("stopped");
        }

    }

    public boolean isStoppedOnError() {
        return stoppedOnError;
    }
    

    
}