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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

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
        new ParametrizedFragment(getTemplateManager())
            .writeTo(getWriter())
            .render(new int[] { -2, 0, 15 });
        checkOutput("-0+");
    }

}
