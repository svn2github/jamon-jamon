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

import org.jamon.JamonException;

public class IllegalAliasTest
    extends TestBase
{
    public void testCircularAlias()
        throws Exception
    {
        expectTemplateException("CircularAlias", "Unknown alias bar", 2, 10);
    }

    public void testUnknownAlias()
        throws Exception
    {
        expectTemplateException("UnknownAlias", "Unknown alias foo", 4, 4);
    }

    public void testDuplicateAlias()
        throws Exception
    {
        expectTemplateException("DuplicateAlias", "Duplicate alias foo", 3, 3);
    }
}
