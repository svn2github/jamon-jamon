package org.jamon.integration;

import test.jamon.Arguments;
import test.jamon.OptionalArguments;

/**
 * Test Jamon's parameterized templates.  See "Jamon User's Guide",
 * section 5.
 **/

public class ArgumentTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new Arguments(getTemplateManager())
            .setWriter(getWriter())
            .render(INT, BOOLEAN, STRING);
        checkOutput("" + INT + BOOLEAN + STRING);
    }


    public void testOptional1()
        throws Exception
    {
        new OptionalArguments(getTemplateManager())
            .setWriter(getWriter())
            .setI(INT)
            .render(BOOLEAN, STRING);
        checkOutput("" + INT + BOOLEAN + STRING);
    }

    public void testOptional2()
        throws Exception
    {
        new OptionalArguments(getTemplateManager())
            .setWriter(getWriter())
            .render(BOOLEAN, STRING);
        checkOutput("" + 0 + BOOLEAN + STRING);
    }

    private static final int INT = 3;
    private static final boolean BOOLEAN = true;
    private static final String STRING = "foobar";

}
