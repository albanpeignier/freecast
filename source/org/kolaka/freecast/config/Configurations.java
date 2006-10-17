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

package org.kolaka.freecast.config;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;

public class Configurations {

  public static Configuration subset(Configuration parent, String prefix) {
    Configuration child = parent.subset(prefix);
    return initFrom(child, parent);
  }

  public static HierarchicalConfiguration subset(HierarchicalConfiguration parent, String prefix) {
     return (HierarchicalConfiguration) subset((Configuration) parent, prefix);
  }

  public static Configuration initFrom(Configuration child, Configuration parent) {
     if (child instanceof HierarchicalConfiguration && parent instanceof HierarchicalConfiguration) {
       initFrom((HierarchicalConfiguration) child, (HierarchicalConfiguration) parent);
     } else  if (child instanceof AbstractConfiguration && parent instanceof AbstractConfiguration) {
       initFrom((AbstractConfiguration) child, (AbstractConfiguration) parent);
     } else {
       throw new IllegalArgumentException("Unsupported configurations : " + child + "/" + parent);
     }
     return child;
  }

  public static AbstractConfiguration initFrom(AbstractConfiguration child, AbstractConfiguration parent) {
    child.setListDelimiter(parent.getListDelimiter());
    child.setDelimiterParsingDisabled(parent.isDelimiterParsingDisabled());
    child.setThrowExceptionOnMissing(parent.isThrowExceptionOnMissing());
    return child;
  }
  
  public static HierarchicalConfiguration initFrom(HierarchicalConfiguration child, HierarchicalConfiguration parent)
  {
    initFrom((AbstractConfiguration) child, (AbstractConfiguration) parent);
    child.setExpressionEngine(parent.getExpressionEngine());
    return child;
  }
  
}
