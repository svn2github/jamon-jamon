/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.ant;

import org.apache.tools.ant.Location;

public class JamonLocation extends Location {
  public JamonLocation(org.jamon.api.Location location) {
    super(location.getTemplateLocation().toString(), location.getLine(), location.getColumn());
    columnNumber = location.getColumn();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(super.toString());
    buf.insert(buf.length() - 2, ":");
    buf.insert(buf.length() - 2, columnNumber);
    return buf.toString();
  }

  private final int columnNumber;

  private static final long serialVersionUID = 2007052301L;
}
