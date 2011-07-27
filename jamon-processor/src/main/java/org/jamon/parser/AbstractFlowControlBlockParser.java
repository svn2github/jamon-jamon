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
