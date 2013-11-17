/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.compiler;

import org.jamon.api.Location;
import org.jamon.api.ParserError;

/**
 * @author ian
 **/
public class ParserErrorImpl extends Exception implements ParserError {
  public ParserErrorImpl(Location location, String message) {
    if ((this.location = location) == null || (this.message = message) == null) {
      throw new NullPointerException();
    }
  }

  /**
   * @return The location of the error
   */
  @Override
  public Location getLocation() {
    return location;
  }

  /**
   * @return The error message
   */
  @Override
  public String getMessage() {
    return message;
  }

  private final Location location;

  private final String message;

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof ParserErrorImpl
      && location.equals(((ParserErrorImpl) obj).location)
      && message.equals(((ParserErrorImpl) obj).message);
  }

  @Override
  public int hashCode() {
    return location.hashCode() ^ message.hashCode();
  }

  @Override
  public String toString() {
    return getLocation().getTemplateLocation() + ":" + getLocation().getLine() + ":"
      + getLocation().getColumn() + ": " + getMessage();
  }

  private static final long serialVersionUID = 2006091701L;
}
