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
 * Contributor(s): Ian Robertson
 */

package org.jamon.integration;

import test.jamon.Arguments;
import test.jamon.OptionalArguments;
import test.jamon.OptionalDefArguments;

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
            .render(getWriter(), INT, BOOLEAN, STRING);
        checkOutput("" + INT + BOOLEAN + STRING);
    }


    public void testOptional1()
        throws Exception
    {
        new OptionalArguments(getTemplateManager())
            .setI(INT)
            .render(getWriter(), BOOLEAN, STRING);
        checkOutput("" + INT + BOOLEAN + STRING);
    }

    public void testOptional2()
        throws Exception
    {
        new OptionalArguments(getTemplateManager())
            .render(getWriter(), BOOLEAN, STRING);
        checkOutput("" + 0 + BOOLEAN + STRING);
    }

    public void testOptionalDef()
        throws Exception
    {
        new OptionalDefArguments(getTemplateManager()).render(getWriter());
        checkOutput("" + 0 + BOOLEAN + "s"
                    + "\n" + "1" + BOOLEAN + "s");
    }

    private static final int INT = 3;
    private static final boolean BOOLEAN = true;
    private static final String STRING = "foobar";

}
