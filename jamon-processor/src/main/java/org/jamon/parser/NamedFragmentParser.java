package org.jamon.parser;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.NamedFragmentNode;

public class NamedFragmentParser extends AbstractBodyParser<NamedFragmentNode> {
  public static final String NAMED_FRAGMENT_CLOSE_EXPECTED =
    "Reached end of file while inside a named call fragment; '</|>' expected";

  public NamedFragmentParser(
    NamedFragmentNode rootNode, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(rootNode, reader, errors);
  }

  @Override
  protected void handleEof() {
    addError(bodyStart, NAMED_FRAGMENT_CLOSE_EXPECTED);
  }

  @Override
  protected boolean handleNamedFragmentClose(Location tagLocation) {
    return true;
  }
}
