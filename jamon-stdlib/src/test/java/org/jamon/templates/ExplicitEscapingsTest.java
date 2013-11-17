/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.templates;

import org.jamon.stdlib.tests.ExplicitEscapings;

/**
 * Test Jamon's escaping mechanisms.
 **/

public class ExplicitEscapingsTest
    extends TestBase
{
    public void testIt()
        throws Exception
    {
        new ExplicitEscapings().render(getWriter());
        checkOutput("HTML:\nit's \"quoted\" &amp; &lt;b&gt;bold&lt;/b&gt;\n\n"
                    + "XML:\nit&apos;s &quot;quoted&quot; &amp; &lt;b&gt;bold&lt;/b&gt;\n\n"
                    + "URL:\nit%27s+%22quoted%22+%26+%3Cb%3Ebold%3C%2Fb%3E%0A\n"
                    + "Strict HTML:\nit&#39;s &quot;quoted&quot; &amp; &lt;b&gt;bold&lt;/b&gt;\n\n"
                    + "Double HTML:\nit's \"quoted\" &amp;amp; &amp;lt;b&amp;gt;bold&amp;lt;/b&amp;gt;\n");
    }
}
