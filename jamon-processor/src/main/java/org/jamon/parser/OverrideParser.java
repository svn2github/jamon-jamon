/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.OverrideNode;

public class OverrideParser extends SubcomponentParser<OverrideNode> {
  /**
   * @param reader
   * @param errors
   */
  public OverrideParser(
    String name, Location tagLocation, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(new OverrideNode(tagLocation, name), reader, errors);
  }

  @Override
  protected String tagName() {
    return "override";
  }

  @Override
  protected void handleParentArgsNode(Location tagLocation) throws IOException {
    root.addSubNode(new ParentArgsParser(reader, errors, tagLocation).getParentArgsNode());
  }
}
