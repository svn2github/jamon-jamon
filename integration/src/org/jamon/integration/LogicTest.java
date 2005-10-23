/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.integration;

import java.util.ArrayList;
import test.jamon.IterateTest;
import test.jamon.IfTest;
import test.jamon.IfElseTest;

/**
 * Test Jamon's standard library logic templates.
 **/

public class LogicTest
    extends TestBase
{
    public void testIterateEmpty()
        throws Exception
    {
        new IterateTest().render(getWriter(), new ArrayList().iterator());
        checkOutput("");
    }

    public void testIterateNotEmpty()
        throws Exception
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
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
