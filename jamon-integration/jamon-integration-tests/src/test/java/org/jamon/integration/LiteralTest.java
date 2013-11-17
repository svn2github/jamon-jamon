/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import test.jamon.Literal;

public class LiteralTest
    extends TestBase
{
    public void testLiteral()
        throws Exception
    {
        new Literal().render(getWriter());
        checkOutput("<%args>xxx</%args></%def><%def>\n"
                    +"<%import><%java>\\\n"
                    +"</%java></%import>\n"
                    +"</%LITERAL\n"
                    +"% x = 5;\n\n");
    }
}
