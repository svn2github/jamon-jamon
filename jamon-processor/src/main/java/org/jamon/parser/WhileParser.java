/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.WhileNode;

public class WhileParser extends AbstractFlowControlBlockParser<WhileNode> {

  public WhileParser(WhileNode node, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(node, reader, errors);
  }

  @Override
  protected String tagName() {
    return "while";
  }

}
