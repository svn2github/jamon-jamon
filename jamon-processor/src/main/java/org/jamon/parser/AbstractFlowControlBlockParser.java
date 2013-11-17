/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractBodyNode;

public abstract class AbstractFlowControlBlockParser<Node extends AbstractBodyNode> extends
    SubcomponentParser<Node> {
  public AbstractFlowControlBlockParser(
    Node node, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(node, reader, errors);
  }

  @Override
  protected void handlePostTag() {}
}
