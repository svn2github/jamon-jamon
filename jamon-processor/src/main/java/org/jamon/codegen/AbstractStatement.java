/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.api.Location;

public abstract class AbstractStatement implements Statement {
  protected AbstractStatement(Location location, String templateIdentifier) {
    this.location = location;
    this.templateIdentifier = templateIdentifier;
  }

  private final Location location;

  private final String templateIdentifier;

  protected final Location getLocation() {
    return location;
  }

  protected final String getTemplateIdentifier() {
    return templateIdentifier;
  }

  protected final void generateSourceLine(CodeWriter writer) {
    writer.printLocation(location);
  }

}
