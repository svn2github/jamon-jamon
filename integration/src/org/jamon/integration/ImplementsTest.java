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
 * Contributor(s):
 */

package org.jamon.integration;

import test.jamon.Implements;

/**
 * Test Jamon's implements.
 **/

public class ImplementsTest
    extends TestBase
{
    public void testImplements()
        throws Exception
    {
        Implements i = new Implements();
        i.setX(INT);
        SomeInterface x = i;
        x.render(getWriter(), STRING);
        checkOutput("" + INT + STRING);


        // FIXME: want to do

        // SomeInterface x = new Implements();
        // x.writeTo(getWriter()).setX(INT).render(STRING);
    }

    private static final int INT = 3;
    private static final String STRING = "foobar";

}
