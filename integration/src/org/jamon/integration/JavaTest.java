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

import test.jamon.JavaEscape;

/**
 * Test Jamon's java escapes.  See "Jamon User's Guide", section 2.
 **/

public class JavaTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new JavaEscape(getTemplateManager())
            .writeTo(getWriter())
            .render();
        checkOutput("0\n1\n2\n");
    }

}
