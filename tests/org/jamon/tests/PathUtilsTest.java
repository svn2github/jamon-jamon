package org.modusponens.tests.jtt;

import junit.framework.TestCase;
import org.modusponens.jtt.PathUtils;

public class PathUtilsTest
    extends TestCase
{
    public PathUtilsTest(String p_name)
    {
        super(p_name);
    }

    public void testPathToClassName()
        throws Exception
    {
        assertEquals("a.b.c", PathUtils.pathToClassName("/a/b/c"));
        assertEquals("a.b.c", PathUtils.pathToClassName("a/b/c"));
        assertEquals("a", PathUtils.pathToClassName("a"));
        assertEquals("a", PathUtils.pathToClassName("/a"));
        assertEquals("axy.bxy.cxyasd",
                     PathUtils.pathToClassName("/axy/bxy/cxyasd"));
    }

    public void testClassNameToPath()
        throws Exception
    {
        assertEquals("/a/b/c", PathUtils.classNameToPath("a.b.c"));
        assertEquals("/a", PathUtils.classNameToPath("a"));
        assertEquals("/axy/bxy/cxyasd",
                     PathUtils.classNameToPath("axy.bxy.cxyasd"));
    }
}
