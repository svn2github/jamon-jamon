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

package org.jamon.tests.util;

import java.io.File;
import junit.framework.TestCase;
import org.jamon.util.StringUtils;

public class StringUtilsTest
    extends TestCase
{
    public void testPathToClassName()
        throws Exception
    {
        assertEquals("a.b.c", StringUtils.templatePathToClassName("/a/b/c"));
        assertEquals("a.b.c", StringUtils.templatePathToClassName("a/b/c"));
        assertEquals("a", StringUtils.templatePathToClassName("a"));
        assertEquals("a", StringUtils.templatePathToClassName("/a"));
        assertEquals("axy.bxy.cxyasd",
                     StringUtils.templatePathToClassName("/axy/bxy/cxyasd"));
    }

    public void testClassNameToPath()
        throws Exception
    {

        assertEquals(File.separator
                     + "a"
                     + File.separator
                     + "b"
                     + File.separator
                     + "c",
                     StringUtils.classNameToFilePath("a.b.c"));
        assertEquals(File.separator + "a",
                     StringUtils.classNameToFilePath("a"));
        assertEquals(File.separator
                     + "axy"
                     + File.separator
                     + "bxy"
                     + File.separator
                     + "cxyasd",
                     StringUtils.classNameToFilePath("axy.bxy.cxyasd"));
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

    public void testHexify()
        throws Exception
    {
        assertEquals("0045", StringUtils.hexify4(0x45));
        assertEquals("fe32", StringUtils.hexify4(0xFE32));
        assertEquals("0003", StringUtils.hexify4(3));
        assertEquals("0000", StringUtils.hexify4(0));
        assertEquals("ffff", StringUtils.hexify4(0xffff));
        assertEquals("03b8", StringUtils.hexify4(0x03b8));
    }

}
