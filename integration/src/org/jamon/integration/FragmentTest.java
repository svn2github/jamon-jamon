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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):Ian Robertson
 */

package org.jamon.integration;

import test.jamon.Fragment;
import test.jamon.EscapedFragment;
import test.jamon.RenderedFragment;
import test.jamon.RepeatedFragmentName;

/**
 * Test Jamon's template fragments.  See "Jamon User's Guide", section 8.
 **/

public class FragmentTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new Fragment(getTemplateManager()).render(getWriter(), 1);
        checkOutput("1(2)1");
    }

    public void testMakeRenderer()
        throws Exception
    {
        new RenderedFragment(getTemplateManager()).render(getWriter());
        checkOutput("<");
    }

    public void testEscaping()
        throws Exception
    {
        new EscapedFragment(getTemplateManager()).render(getWriter());
        checkOutput("<&lt;");
    }

    public void testRepeatedFragmentNameExercise()
        throws Exception
    {
        new RepeatedFragmentName(getTemplateManager()).render(getWriter());
        checkOutput("d1:d1,d2:d2,d3:d3 - 7");
    }

    public void testFragmentArgInNamedFragmentImpl()
        throws Exception
    {
        expectTemplateException(
            "test/jamon/broken/FragmentArgInNamedFragmentImpl",
            "Fragment args for fragments not implemented",
            3,
            3);
    }

    public void testFragmentArgInAnonFragmentImpl()
        throws Exception
    {
        expectTemplateException(
            "test/jamon/broken/FragmentArgInAnonFragmentImpl",
            "Fragment args for fragments not implemented",
            3,
            3);
    }


}
