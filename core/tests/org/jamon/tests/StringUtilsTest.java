package org.modusponens.tests.jtt;

import junit.framework.TestCase;

import org.jamon.StringUtils;

public class StringUtilsTest
    extends TestCase
{
    public StringUtilsTest(String p_name)
    {
        super(p_name);
    }

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
