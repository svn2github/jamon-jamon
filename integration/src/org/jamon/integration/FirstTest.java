package org.jamon.integration;

import java.math.BigDecimal;

import gnu.regexp.RE;

import test.jamon.TestTemplate;

public class FirstTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new TestTemplate(getTemplateManager())
            .setWriter(getWriter())
            .setX(57)
            .render(new BigDecimal("34.5324"));
        checkOutput(new RE(".*"
                           +"An external template with a "
                           +"parameterized fragment parameter \\(farg\\)"
                           + "\\s*"
                           + "i is 3 and s is yes."
                           + "\\s*"
                           + "i is 7 and s is no.*",
                           RE.REG_DOT_NEWLINE));
    }

}
