package org.jamon.integration;

/**
 * Test Jamon's escape mechanisms.
 **/

public class EscapeTest
    extends TestBase
{
    public void testExercise()
        throws Exception
    {
        new test.jamon.Escapes(getTemplateManager())
            .writeTo(getWriter())
            .render();
        checkOutput("This is how to escape a newline in Java: \\n\nThis is how to escape a newline in Java: \\\"\nAnd this mess \\\" \\n \\\\ is on one line.\n");
    }

}
