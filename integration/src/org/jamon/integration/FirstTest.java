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

import java.math.BigDecimal;

import gnu.regexp.RE;

import test.jamon.TestTemplate;

public class FirstTest
    extends TestBase
{

    public void testExercise()
        throws Exception
    {
        new TestTemplate(getTemplateManager())
            .writeTo(getWriter())
            .setX(57)
            .render(new BigDecimal("34.5324"));
        checkOutput(new RE(".*"
                           +"An external template with a "
                           +"parameterized fragment parameter \\(farg\\)"
                           + "\\s*"
                           + "i is 3 and s is yes."
                           + "\\s*"
                           + "i is 7 and s is no.*",
                           RE.REG_DOT_NEWLINE));
    }

}
