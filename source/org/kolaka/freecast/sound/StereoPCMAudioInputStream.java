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

package org.kolaka.freecast.sound;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * Transforms a mono <code>AudioInputStream</code> into a stereo one. 
 * Freely inspired from 
 * <a href="http://www.jsresources.org/examples/SingleChannelStereoAudioInputStream.java.html">SingleChannelStereoAudioInputStream.java</a>.
 * 
 * @author <a href="mailto:alban.peignier@free.fr">Alban Peignier</a>
 */
public class StereoPCMAudioInputStream extends AudioInputStream {

    private AudioInputStream inputStream;
    private final int frameSize;
    private final int sourceFrameSize;

    public StereoPCMAudioInputStream(AudioInputStream sourceStream) {
        super(new ByteArrayInputStream(new byte[0]), new AudioFormat(
                sourceStream.getFormat().getSampleRate(), sourceStream
                        .getFormat().getSampleSizeInBits(), 2,
                sourceStream.getFormat().getEncoding().equals(
                        AudioFormat.Encoding.PCM_SIGNED), sourceStream
                        .getFormat().isBigEndian()), sourceStream
                .getFrameLength());

        AudioFormat sourceFormat = sourceStream.getFormat();
        if (!isPcm(sourceFormat.getEncoding())) {
            throw new IllegalArgumentException("source stream has to be PCM");
        }

        if (sourceFormat.getChannels() != 1) {
            throw new IllegalArgumentException("source stream has to mono");
        }

        inputStream = sourceStream;
        frameSize = getFormat().getFrameSize();
        sourceFrameSize = inputStream.getFormat().getFrameSize();
    }

    public static boolean isPcm(AudioFormat.Encoding encoding) {
        return encoding.equals(AudioFormat.Encoding.PCM_SIGNED)
                || encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED);
    }

    public int read() throws IOException {
        throw new IOException("cannot read fraction of a frame");
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        int sampleSizeInBytes = frameSize / 2;
        if ((length % frameSize) != 0) {
            throw new IOException("cannot read fraction of a frame");
        }

        int frames = length / frameSize;
        int lengthToRead = frames * sourceFrameSize;
        byte[] readBuffer = new byte[lengthToRead];

        int readLength = inputStream.read(readBuffer, 0, lengthToRead);

        if (readLength == -1) {
            return -1;
        }

        frames = readLength / sourceFrameSize;
        int writeIndex = offset;
        int readIndex = 0;
        int n;
        if (sampleSizeInBytes == 2) {
            for (int i = 0; i < frames; i++) {
                int leftIndex = writeIndex;
                int rightIndex = writeIndex + sampleSizeInBytes;

                buffer[leftIndex] = buffer[rightIndex] = readBuffer[readIndex];
                buffer[leftIndex + 1] = buffer[rightIndex + 1] = readBuffer[readIndex + 1];

                writeIndex += frameSize;
                readIndex += sourceFrameSize;
            }
        } else {
            for (int i = 0; i < frames; i++) {
                int leftIndex = writeIndex;
                int rightIndex = writeIndex + sampleSizeInBytes;

                n = 0;
                while (n < sampleSizeInBytes) {
                    buffer[leftIndex + n] = buffer[rightIndex + n] = readBuffer[readIndex
                            + n];
                    n++;
                }

                writeIndex += frameSize;
                readIndex += sourceFrameSize;
            }
        }

        return frames * frameSize;
    }

    public long skip(long length) throws IOException {
        long bytesInSource = (length / frameSize) * sourceFrameSize;
        long bytesSkippedInSource = inputStream.skip(bytesInSource);
        return (bytesSkippedInSource / sourceFrameSize) * frameSize;
    }

    public int available() throws IOException {
        int availableInSource = inputStream.available();
        return (availableInSource / sourceFrameSize) * frameSize;
    }

    public void close() throws IOException {
        inputStream.close();
    }

    public void mark(int readLimit) {
        int sourceReadLimit = (readLimit / frameSize) * sourceFrameSize;
        inputStream.mark(sourceReadLimit);
    }

    public void reset() throws IOException {
        inputStream.reset();
    }

    public boolean markSupported() {
        return inputStream.markSupported();
    }

}
