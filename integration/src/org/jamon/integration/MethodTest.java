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

import test.jamon.Method;
import test.jamon.MethodChild;
import test.jamon.MethodOverride;

public class MethodTest
    extends TestBase
{
    public void testSimpleMethod()
        throws Exception
    {
        new Method(getTemplateManager()).render(getWriter());
        checkOutput("{foo: req1, po1} {foo: req1, po2} {bar: passed 1}");
    }

    public void testInheritedMethod()
        throws Exception
    {
        new MethodChild(getTemplateManager()).render(getWriter());
        final String methodsOutput =
            "{foo: req1, po1} {foo: req1, po2} {bar: passed 1}";
        checkOutput("{ parent: " + methodsOutput + " }{ child: "
                    + methodsOutput + " }");
    }

    public void testOverriddenMethod()
        throws Exception
    {
        new MethodOverride(getTemplateManager()).render(getWriter());
        final String methodsOutput =
            "{fooOverride: req1, co3} {fooOverride: req1, po2}"
            + " {barOverride: passed 2}";
        checkOutput("{ parent: " + methodsOutput + " }{ child: "
                    + methodsOutput + " }");
    }

    public void testOverrideNonexistentMethod()
        throws Exception
    {
        expectTemplateException(
            "OverrideNonexistentMethod",
            "There is no such method noSuchMethod to override",
            2, 12);
    }
}
