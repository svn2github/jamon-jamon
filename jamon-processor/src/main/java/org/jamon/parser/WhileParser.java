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
