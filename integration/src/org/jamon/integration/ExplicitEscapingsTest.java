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

import test.jamon.ExplicitEscapings;

/**
 * Test Jamon's escaping mechanisms.
 **/

public class ExplicitEscapingsTest
    extends TestBase
{
    public void testIt()
        throws Exception
    {
        ExplicitEscapings escapings =
            new ExplicitEscapings(getTemplateManager())
            .writeTo(getWriter());
        escapings.render();
        checkOutput("HTML:\nit's \"quoted\" &amp; &lt;b&gt;bold&lt;/b&gt;\n\n"
                    + "XML:\nit&apos;s &quot;quoted&quot; &amp; &lt;b&gt;bold&lt;/b&gt;\n\n"
                    + "URL:\nit%27s+%22quoted%22+%26+%3Cb%3Ebold%3C%2Fb%3E%0A\n"
                    + "Strict HTML:\nit&#39;s &quot;quoted&quot; &amp; &lt;b&gt;bold&lt;/b&gt;\n\n"
                    + "Double HTML:\nit's \"quoted\" &amp;amp; &amp;lt;b&amp;gt;bold&amp;lt;/b&amp;gt;\n");
    }
}
