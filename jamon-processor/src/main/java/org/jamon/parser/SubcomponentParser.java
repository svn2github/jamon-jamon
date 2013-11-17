/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
