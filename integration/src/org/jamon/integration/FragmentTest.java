package org.jamon.integration;

import test.jamon.Fragment;

/**
 * Test Jamon's template fragments.  See "Jamon User's Guide", section 8.
 **/

public class FragmentTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new Fragment(getTemplateManager())
            .setWriter(getWriter())
            .render(1);
        checkOutput("1(2)1");
    }

}
