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

import java.io.IOException;

/**
 * Test Jamon's encoding mechanisms.
 **/

public class EncodingsTest
    extends TestBase
{
    public void testDefault()
        throws Exception
    {
        new test.jamon.Encodings(getTemplateManager())
            .writeTo(getWriter())
            .render();
        checkOutput("Default encoding", "&lt;&gt;&amp;&#34;&#39;" + 
                                        "&lt;&gt;&amp;&#34;&#39;" + 
                                        "&lt;&gt;&amp;&#34;&#39;");
    }

    public void testNone()
        throws IOException
    {
        new test.jamon.Encodings(getTemplateManager())
            .writeTo(getWriter())
            .encoding(org.jamon.Encoding.NONE)
            .render();
        checkOutput("Encoding NONE", "<>&\"'" + 
                                     "<>&\"'" + 
                                     "<>&\"'");
    }

}
