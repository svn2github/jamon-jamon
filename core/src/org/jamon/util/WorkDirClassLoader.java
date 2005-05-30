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

package org.jamon.util;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WorkDirClassLoader
    extends ClassLoader
{
    public WorkDirClassLoader(ClassLoader p_parent, String p_workDir)
    {
        super(p_parent);
        m_workDir = p_workDir;
    }

    private final String m_workDir;

    private File getFileForClass(String p_name)
    {
        return new File(m_workDir,
                        StringUtils.classNameToFilePath(p_name)
                        + ".class");
    }

    public synchronized void invalidate()
    {
        m_loader = null;
    }


    private class Loader extends ClassLoader
    {
        Loader()
        {
            super(WorkDirClassLoader.this);
        }

        @Override public String toString()
        {
            return super.toString() + " { " + " parent: " + getParent() + " }";
        }

        private final Map<String, Class> m_cache = new HashMap<String, Class>();

        private byte [] readBytesForClass(String p_name)
            throws IOException
        {
            FileInputStream s =
                new FileInputStream(getFileForClass(p_name));
            try
            {
                final byte [] buf = new byte[1024];
                byte [] bytes = new byte[0];
                while (true)
                {
                    int i = s.read(buf);
                    if (i <= 0)
                    {
                        break;
                    }
                    byte [] newbytes = new byte[bytes.length + i];
                    System.arraycopy(bytes,0,newbytes,0,bytes.length);
                    System.arraycopy(buf,0,newbytes,bytes.length,i);
                    bytes = newbytes;
                }
                return bytes;
            }
            finally
            {
                s.close();
            }
        }

        @Override protected Class<?> loadClass(String p_name, boolean p_resolve)
            throws ClassNotFoundException
        {
            if (! getFileForClass(p_name).exists())
            {
                return super.loadClass(p_name, p_resolve);
            }
            else
            {
                Class c = m_cache.get(p_name);
                if (c == null)
                {
                    try
                    {
                        byte [] code = readBytesForClass(p_name);
                        c = this.defineClass(p_name, code, 0, code.length);
                        if (p_resolve)
                        {
                            this.resolveClass(c);
                        }
                        m_cache.put(p_name, c);
                    }
                    catch (IOException e)
                    {
                        throw new ClassNotFoundException(e.getMessage());
                    }
                }
                return c;
            }
        }

    }

    @Override
    protected synchronized Class<?> loadClass(String p_name, boolean p_resolve)
        throws ClassNotFoundException
    {
        if (! getFileForClass(p_name).exists())
        {
            return super.loadClass(p_name, p_resolve);
        }
        else
        {
            if (m_loader == null)
            {
                m_loader = new Loader();
            }
            return m_loader.loadClass(p_name, p_resolve);
        }
    }

    @Override public String toString()
    {
        return super.toString() + " { workDir: " + m_workDir
            + "; parent: " + getParent() + " }";
    }

    private Loader m_loader;
}
