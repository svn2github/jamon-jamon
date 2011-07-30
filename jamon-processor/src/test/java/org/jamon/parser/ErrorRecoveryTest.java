package org.jamon.parser;

import org.junit.Test;

public class ErrorRecoveryTest extends AbstractParserTest {
  @Test
  public void testCloseTagRecovery() throws Exception {
    assertErrorPair(
      "</%foo></%bar>",
      1, 1, "Unexpected tag close </%foo>",
      1, 8, "Unexpected tag close </%bar>");
    assertErrorPair(
      "<%def bob></%foo></%def>",
      1, 11, "Unexpected tag close </%foo>",
      1, 18, "Unexpected tag close </%def>");
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(ErrorRecoveryTest.class);
  }
}
