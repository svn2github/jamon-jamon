package org.jamon.integration;

import test.jamon.WriterGrab;

/**
 * Test Jamon's java escapes.  See "Jamon User's Guide", section 2.
 **/

public class WriterTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new WriterGrab(getTemplateManager())
            .writeTo(getWriter())
            .render();
        checkOutput("SECRET MESSAGE\n");
    }

}
