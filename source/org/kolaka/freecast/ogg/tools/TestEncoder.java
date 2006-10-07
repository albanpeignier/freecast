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

package org.kolaka.freecast.ogg.tools;

import java.io.EOFException;
import java.io.IOException;
import java.net.URL;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.Application;
import org.kolaka.freecast.ogg.EncoderOggSource;
import org.kolaka.freecast.ogg.OggPage;
import org.kolaka.freecast.sound.AudioSystem;
import org.kolaka.freecast.transport.receiver.FilePlaylist;
import org.kolaka.freecast.transport.receiver.Playlist;
import org.kolaka.freecast.transport.receiver.Playlist.Entry;

public class TestEncoder extends Application {
  
  public TestEncoder() {
    super("testplayer");
  }
  
  public static void main(String[] args) {
    new TestEncoder().run(args);
  }
  
  private URL url;
  private boolean encode;

  protected void postInit(HierarchicalConfiguration configuration) throws Exception {
      url = new URL( configuration.getString("playlist") );
      encode = configuration.getBoolean("encode", true);
  }
  
  protected void run() throws Exception {
    Playlist playlist = new FilePlaylist(url);
    AudioFormat readFormat = new AudioFormat(44100, 16, 2, true, false);
    
    for (int i = 0; i < playlist.size(); i++) {
      Entry entry = playlist.get(i);

      AudioInputStream originalAudioInput = AudioSystem
            .getAudioInputStream(entry.openStream());
      AudioInputStream audioInput = AudioSystem.getAudioInputStream(readFormat,
            originalAudioInput);
      if (encode) {
        encode(entry.getDescription(), audioInput);
      } else {
        read(entry.getDescription(), audioInput);
      }
      audioInput.close();
    }
  }

  private void read(String description, AudioInputStream audioInput) throws IOException {
    CRC32 checksum = new CRC32();
    IOUtils.copy(audioInput, new CheckedOutputStream(new NullOutputStream(), checksum));
    LogFactory.getLog(getClass()).info(description  + ": " + Long.toHexString(checksum.getValue()));
  }
  
  private void encode(String description, AudioInputStream audioInput) throws IOException {
    CRC32 checksum = new CRC32();
    EncoderOggSource source = new EncoderOggSource(audioInput, description, 0);
    while (true) {
      try {
        OggPage page = source.next();
        LogFactory.getLog(getClass()).debug("encoded page: " + page);
        checksum.update(page.getRawBytes());
      } catch (EOFException e) {
        LogFactory.getLog(getClass()).debug("end of stream", e);
        break;
      }
    }    
    source.close();
    LogFactory.getLog(getClass()).info(description  + ": " + Long.toHexString(checksum.getValue()));
  }
  
  
}
