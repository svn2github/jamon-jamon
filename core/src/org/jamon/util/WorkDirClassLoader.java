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
        throws IOException
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
        m_classMap.clear();
    }


    private class Loader extends ClassLoader
    {
        Loader()
        {
            super(WorkDirClassLoader.this);
        }
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

        protected Class loadClass(String p_name, boolean p_resolve)
            throws ClassNotFoundException
        {
            if (! getFileForClass(p_name).exists())
            {
                return super.loadClass(p_name, p_resolve);
            }
            else
            {
                try
                {
                    byte [] bytecode = readBytesForClass(p_name);
                    return this.defineClass(p_name,bytecode,0,bytecode.length);
                }
                catch (IOException e)
                {
                    throw new ClassNotFoundException(e.getMessage());
                }
            }
        }

    }

    protected synchronized Class loadClass(String p_name, boolean p_resolve)
        throws ClassNotFoundException
    {
        if (! getFileForClass(p_name).exists())
        {
            return super.loadClass(p_name, p_resolve);
        }

        Class c = (Class) m_classMap.get(p_name);
        if (c != null)
        {
            return c;
        }
        if (m_loader == null)
        {
            m_loader = new Loader();
        }

        c = m_loader.loadClass(p_name);
        if (p_resolve)
        {
            resolveClass(c);
        }
        m_classMap.put(p_name,c);
        return c;
    }

    private ClassLoader m_loader;
    private final Map m_classMap = new HashMap();
}
