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

package org.jamon;

import junit.framework.TestCase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.net.URL;

public class ResourceTemplateSourceTest
    extends TestCase
{
    private final String PREFIX = "foo";
    private File m_tmpDir;
    private File m_subDir;

    protected void setUp()
        throws Exception
    {
        m_tmpDir = File.createTempFile("jrt","");
        m_tmpDir.delete();
        m_tmpDir.mkdirs();
        m_subDir = new File(m_tmpDir, PREFIX);
        m_subDir.mkdirs();
    }

    protected void tearDown()
        throws Exception
    {
        fullDelete(m_tmpDir);
    }

    private static void fullDelete(File p_directory)
    {
        File[] files = p_directory.listFiles();
        if( files != null )
        {
            for( int i = 0; i < files.length; ++i )
            {
                if( files[i].isDirectory() )
                {
                    fullDelete( files[i] );
                }
                else
                {
                    files[i].delete();
                }
            }
        }
        p_directory.delete();
    }


    public void testResources()
        throws Exception
    {
        ResourceTemplateSource source = new ResourceTemplateSource(new URLClassLoader(new URL[] { m_tmpDir.toURL() }), PREFIX);
        final String pkg = "pkg";
        File pkgDir = new File(m_subDir, pkg);
        pkgDir.mkdirs();
        File f = File.createTempFile("abcd",".jamon", pkgDir);
        FileOutputStream fos = new FileOutputStream(f);
        final byte[] contents = "This is a test.\n".getBytes("US-ASCII");
        fos.write(contents);
        fos.close();
        final long now = System.currentTimeMillis() / 1000l;
        String tName = f.getName();
        int j = tName.indexOf(".");
        tName = tName.substring(0, j);
        String tmpl = "/" + pkg + "/" + tName;
        assertTrue(source.available(tmpl));
        assertFalse(source.available(tmpl + "x"));
        assertTrue(Math.abs(now - source.lastModified(tmpl) / 1000l) <= 1);
        byte[] read = new byte[contents.length];
        InputStream is = null;
        try
        {
            is = source.getStreamFor(tmpl);
            assertEquals(read.length, is.read(read));
            assertEquals(-1, is.read());
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
        for (int i = 0; i < read.length; ++i)
        {
            assertEquals(contents[i], read[i]);
        }
    }
}
