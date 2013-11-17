/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.node;

import org.jamon.api.Location;
import org.jamon.api.TemplateLocation;

/**
 * @author ian
 **/
public final class LocationImpl implements Location {
  public LocationImpl(TemplateLocation templateLocation, int line, int column) {
    this.templateLocation = templateLocation;
    this.line = line;
    this.column = column;
  }

  @Override
  public TemplateLocation getTemplateLocation() {
    return templateLocation;
  }

  @Override
  public int getColumn() {
    return column;
  }

  @Override
  public int getLine() {
    return line;
  }

  private final TemplateLocation templateLocation;

  private final int line;

  private final int column;

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LocationImpl) {
      Location loc = (Location) obj;
      return loc.getTemplateLocation().equals(templateLocation) && loc.getLine() == line
        && loc.getColumn() == column;
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return (line * 160 + column) ^ templateLocation.hashCode();
  }

  @Override
  public String toString() {
    return "Location{" + templateLocation + ": (" + line + ":" + column + ")}";
  }
}
