package org.jamon.integration;

import test.jamon.LegacyArrowSyntax;

public class LegacyArrowSyntaxTest extends TestBase {
  public void testExercise() throws Exception {
    new LegacyArrowSyntax().render(getWriter());
    checkOutput("simple simple simple\n12");
  }
}
