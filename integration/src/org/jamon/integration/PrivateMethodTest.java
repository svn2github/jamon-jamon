package org.jamon.integration;

import test.jamon.PrivateMethods;

/**
 * Test Jamon's private methods.  See "Jamon User's Guide", section 6.
 **/

public class PrivateMethodTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new PrivateMethods(getTemplateManager())
            .setWriter(getWriter())
            .render();
        checkOutput("7=1111111");
    }

}
