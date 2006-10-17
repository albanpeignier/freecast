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

package org.kolaka.freecast.setup;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.LogFactory;
import org.kolaka.freecast.transport.receiver.PlaylistEncoderReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.ReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.ShoutClientReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.ShoutServerReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.SourceReceiverConfiguration;
import org.kolaka.freecast.transport.receiver.TestReceiverConfiguration;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;
import org.pietschy.wizard.models.BranchingPath;
import org.pietschy.wizard.models.Condition;
import org.pietschy.wizard.models.MultiPathModel;
import org.pietschy.wizard.models.Path;
import org.pietschy.wizard.models.SimplePath;

public class ReceiverWizardModel extends MultiPathModel {
  
  private String type;
  private SourceReceiverConfiguration configuration;

  public static final String TYPE_DEFAULT = "default";
  public static final String TYPE_PLAYLIST = "playlist";
  public static final String TYPE_SHOUTCLIENT = "shoutclient";
  public static final String TYPE_SHOUTSERVER = "shoutserver";
  
  private static final Map CONFIGURATIONS = MapUtils.putAll(new TreeMap(), 
      new Object[][] {
        { TYPE_DEFAULT, TestReceiverConfiguration.class },
        { TYPE_PLAYLIST, PlaylistEncoderReceiverConfiguration.class },
        { TYPE_SHOUTCLIENT, ShoutClientReceiverConfiguration.class },
        { TYPE_SHOUTSERVER, ShoutServerReceiverConfiguration.class },
      }
    ) ;
  
  public ReceiverWizardModel() {
    super(createPath());
  }

  private static Path createPath() {
    BranchingPath path = new BranchingPath();
    path.addStep(new ReceiverTypeStep());
    
    Path finalStep = new SimplePath(new ReceiverShowStep());
    
    path.addBranch(finalStep, new ConfigurationClassCondition(TestReceiverConfiguration.class));
    
    SimplePath playlistPath = new SimplePath(new ReceiverPlaylistStep());
    path.addBranch(playlistPath, new ConfigurationClassCondition(PlaylistEncoderReceiverConfiguration.class));
    playlistPath.setNextPath(finalStep);

    SimplePath shoutServerPath = new SimplePath(new ReceiverShoutServerStep());
    path.addBranch(shoutServerPath, new ConfigurationClassCondition(ShoutServerReceiverConfiguration.class));
    shoutServerPath.setNextPath(finalStep);

    SimplePath shoutClientPath = new SimplePath(new ReceiverShoutClientStep());
    path.addBranch(shoutClientPath, new ConfigurationClassCondition(ShoutClientReceiverConfiguration.class));
    shoutClientPath.setNextPath(finalStep);

    return path;
  }
  
  static class ConfigurationClassCondition implements Condition {
    
    private final Class configurationClass;
    
    ConfigurationClassCondition(Class configurationClass) {
      this.configurationClass = configurationClass;
    }
    
    public boolean evaluate(WizardModel model) {
      ReceiverConfiguration configuration = ((ReceiverWizardModel) model).getConfiguration();
      return configurationClass.isInstance(configuration);
    }
    
  }
  
  public SourceReceiverConfiguration getConfiguration() {
    return configuration;
  }
  
  public void createConfiguration(String type) throws InvalidStateException {
    this.type = type;
    
    Class configurationClass = (Class) CONFIGURATIONS.get(type);
    
    if (!configurationClass.isInstance(configuration))
      try {
        this.configuration = (SourceReceiverConfiguration) configurationClass.newInstance();
      } catch (Exception e) {
        throw new InvalidStateException("Can't create configuration", e);
      }

    LogFactory.getLog(getClass()).debug("change configuration for " + configuration);
  }
  
  public String getType() {
    return type;
  }

  public void setReceiverConfiguration(SourceReceiverConfiguration configuration) {
    this.configuration = configuration;
    this.type = getType(configuration);
  }

  private String getType(ReceiverConfiguration configuration) {
    if (TestReceiverConfiguration.isInstance(configuration)) {
      return TYPE_DEFAULT;
    }
    
    for (Iterator iter=CONFIGURATIONS.entrySet().iterator(); iter.hasNext(); ) {
      Entry entry = (Entry) iter.next();
      Class configurationClass = (Class) entry.getValue();
      if (configurationClass.isInstance(configuration)) {
        return (String) entry.getKey();
      }
    }
    throw new IllegalArgumentException("Unsupport configuration : " + configuration);
  }
  
}
