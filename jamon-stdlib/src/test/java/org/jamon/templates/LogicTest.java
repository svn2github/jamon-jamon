/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.templates;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jamon.stdlib.tests.IterateTest;
import org.jamon.stdlib.tests.IfTest;
import org.jamon.stdlib.tests.IfElseTest;

/**
 * Test Jamon's standard library logic templates.
 **/

@Deprecated public class LogicTest
    extends TestBase
{
    public void testIterateEmpty()
        throws Exception
    {
        new IterateTest().render(
            getWriter(), Collections.<Object>emptyList().iterator());
        checkOutput("");
    }

    public void testIterateNotEmpty()
        throws Exception
    {
        List<Object> list = Arrays.asList(new Object[] {"a", "b", "c"});
        new IterateTest().render(getWriter(), list.iterator());
        checkOutput("aabbcc");
    }

    public void testIfTrue()
        throws Exception
    {
        new IfTest().render(getWriter(), true);
        checkOutput("then");
    }

    public void testIfFalse()
        throws Exception
    {
        new IfTest().render(getWriter(), false);
        checkOutput("");
    }

    public void testIfElseTrue()
        throws Exception
    {
        new IfElseTest().render(getWriter(), true);
        checkOutput("then");
    }

    public void testIfElseFalse()
        throws Exception
    {
        new IfElseTest().render(getWriter(), false);
        checkOutput("else");
    }
}
