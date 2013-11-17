/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.parser;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.DefNode;

public class DefParser extends SubcomponentParser<DefNode> {
  /**
   * @param reader
   * @param errors
   */
  public DefParser(
    String name, Location p_tagLocation, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(new DefNode(p_tagLocation, name), reader, errors);
  }

  @Override
  protected String tagName() {
    return "def";
  }
}
