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
 * The Original Code is Jamon code, released ??.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.tests;

import junit.framework.TestCase;

import org.jamon.StringUtils;

public class StringUtilsTest
    extends TestCase
{
    public void testPathToClassName()
        throws Exception
    {
        assertEquals("a.b.c", StringUtils.pathToClassName("/a/b/c"));
        assertEquals("a.b.c", StringUtils.pathToClassName("a/b/c"));
        assertEquals("a", StringUtils.pathToClassName("a"));
        assertEquals("a", StringUtils.pathToClassName("/a"));
        assertEquals("axy.bxy.cxyasd",
                     StringUtils.pathToClassName("/axy/bxy/cxyasd"));
    }

    public void testClassNameToPath()
        throws Exception
    {
        assertEquals("/a/b/c", StringUtils.classNameToPath("a.b.c"));
        assertEquals("/a", StringUtils.classNameToPath("a"));
        assertEquals("/axy/bxy/cxyasd",
                     StringUtils.classNameToPath("axy.bxy.cxyasd"));
    }

    public void testCapitalize()
        throws Exception
    {
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("A", StringUtils.capitalize("a"));
        assertEquals("G", StringUtils.capitalize("g"));
        assertEquals("G", StringUtils.capitalize("G"));
        assertEquals("!", StringUtils.capitalize("!"));
        assertEquals("Abcde", StringUtils.capitalize("Abcde"));
        assertEquals("ABCDE", StringUtils.capitalize("ABCDE"));
        assertEquals("Abcde", StringUtils.capitalize("abcde"));
        assertEquals("AbCdE", StringUtils.capitalize("abCdE"));
        assertEquals("1234abcde", StringUtils.capitalize("1234abcde"));
        assertEquals(null, StringUtils.capitalize(null));
    }
}
