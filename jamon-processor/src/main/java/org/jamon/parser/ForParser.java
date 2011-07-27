package org.jamon.parser;

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.ForNode;

public class ForParser extends AbstractFlowControlBlockParser<ForNode> {

  public ForParser(ForNode node, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(node, reader, errors);
  }

  @Override
  protected String tagName() {
    return "for";
  }

}
