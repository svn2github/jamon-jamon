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
 * created by Ian Robertson are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.integration;

import java.io.File;

import junit.framework.TestCase;

public class AbstractTestBase
    extends TestCase
{
    public static void assertEquals(String p_first, String p_second)
    {
        if( showFullContextWhenStringEqualityFails() )
        {
            assertEquals((Object) p_first, (Object) p_second);
        }
        else
        {
            TestCase.assertEquals(p_first, p_second);
        }
    }

    private static boolean showFullContextWhenStringEqualityFails()
    {
        return Boolean.valueOf
            (System.getProperty
             ("org.jamon.integration.verbose","false")).booleanValue();
    }
}
