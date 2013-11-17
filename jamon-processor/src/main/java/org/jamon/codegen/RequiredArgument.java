/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.api.Location;
import org.jamon.node.ArgNode;

public class RequiredArgument extends AbstractArgument {
  public RequiredArgument(ArgNode arg) {
    super(arg);
  }

  public RequiredArgument(String name, String type, Location location) {
    super(name, type, location);
  }

  @Override
  public String toString() {
    return "RequiredArg: {name => " + getName() + ", type => " + getType() + "}";
  }
}
