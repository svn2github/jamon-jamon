/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractBodyNode;

public abstract class SubcomponentParser<Node extends AbstractBodyNode> extends
    AbstractBodyParser<Node> {
  protected SubcomponentParser(
    Node node, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(node, reader, errors);
  }

  @Override
  public AbstractBodyParser<Node> parse() throws IOException {
    handlePostTag();
    super.parse();
    handlePostTag();
    return this;
  }

  protected void handlePostTag() throws IOException {
    soakWhitespace();
  }

  protected abstract String tagName();

  @Override
  protected void handleTagClose(String tagName, Location tagLocation) throws IOException {
    if (!tagName.equals(tagName())) {
      super.handleTagClose(tagName, tagLocation);
    }
    else {
      handlePostTag();
    }
  }

  @Override
  protected void handleEof() {
    addError(bodyStart, makeError(tagName()));
  }

  public static String makeError(String tagName) {
    return "Reached end of file inside a " + tagName + "; </%" + tagName + "> expected";
  }
}
