package org.jamon.integration;

import test.jamon.JavaEscape;

/**
 * Test Jamon's java escapes.  See "Jamon User's Guide", section 2.
 **/

public class JavaTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new JavaEscape.Factory(getTemplateManager())
            .getInstance(getWriter())
            .render();
        checkOutput("0\n1\n2\n");
    }

}
