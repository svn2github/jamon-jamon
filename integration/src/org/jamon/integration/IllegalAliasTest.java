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

import org.jamon.JamonException;

public class IllegalAliasTest
    extends BrokenTestBase
{
    public void testCircularAlias()
        throws Exception
    {
        checkTemplate("test/jamon/broken/CircularAlias",
                      "Unknown alias ");
    }

    public void testUnknownAlias()
        throws Exception
    {
        checkTemplate("test/jamon/broken/UnknownAlias",
                      "Unknown alias ");
    }

    public void testDuplicateAlias()
        throws Exception
    {
        checkTemplate("test/jamon/broken/DuplicateAlias",
                      "Duplicate alias ");
    }

    private void checkTemplate(String p_path, String p_message)
        throws Exception
    {
        try
        {
            generateSource(p_path);
            fail("No exception thrown");
        }
        catch(JamonException e)
        {
            assertTrue(e.getMessage().startsWith(p_message));
        }
    }
}
