package org.jamon.parser;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.UnnamedFragmentNode;

public class UnnamedFragmentParser extends AbstractBodyParser<UnnamedFragmentNode> {
  public static final String FRAGMENT_CLOSE_EXPECTED =
    "Reached end of file while inside a call fragment; '</&>' expected";

  public UnnamedFragmentParser(
    UnnamedFragmentNode rootNode, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(rootNode, reader, errors);
  }

  @Override
  protected boolean handleFragmentsClose(Location tagLocation) {
    return true;
  }

  @Override
  protected void handleEof() {
    addError(bodyStart, FRAGMENT_CLOSE_EXPECTED);
  }
}
