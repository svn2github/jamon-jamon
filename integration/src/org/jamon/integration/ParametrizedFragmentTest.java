package org.jamon.integration;

import test.jamon.ParametrizedFragment;

/**
 * Test Jamon's parametrized template fragments.  See "Jamon User's
 * Guide", section 9.
 **/

public class ParametrizedFragmentTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new ParametrizedFragment.Factory(getTemplateManager())
            .getInstance(getWriter())
            .render(new int[] { -2, 0, 15 });
        checkOutput("-0+");
    }

}
