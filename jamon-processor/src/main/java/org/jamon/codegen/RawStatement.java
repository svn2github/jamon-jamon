/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.api.Location;

public class RawStatement extends AbstractStatement {
  RawStatement(String code, Location token, String templateIdentifier) {
    super(token, templateIdentifier);
    this.code = code;
  }

  @Override
  public void generateSource(CodeWriter writer, TemplateDescriber describer) {
    generateSourceLine(writer);
    writer.println(code);
  }

  private final String code;
}
