package org.modusponens.jtt;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class WorkDirClassLoader
    extends ClassLoader
{
    public WorkDirClassLoader(ClassLoader p_parent, String p_workDir)
        throws IOException
    {
        super(p_parent);
        m_workUrl = new URL("file:" + p_workDir + "/");
    }

    private final URL m_workUrl;

    private String getFileNameForClass(String p_name)
    {
        return m_workUrl.getPath()
            + StringUtils.classNameToPath(p_name.replace('.','/'))
            + ".class";
    }

    public synchronized void invalidate()
    {
        m_loader = null;
        m_classMap.clear();
    }

    protected Class loadClass(String p_name, boolean p_resolve)
        throws ClassNotFoundException
    {
        File cf = new File(getFileNameForClass(p_name));
        if (!cf.exists())
        {
            return super.loadClass(p_name, p_resolve);
        }

        synchronized (this)
        {
            Class c = (Class) m_classMap.get(p_name);
            if (c != null)
            {
                return c;
            }
            if (m_loader == null)
            {
                m_loader = new URLClassLoader(new URL[] { m_workUrl },
                                              getParent());
            }
            c = m_loader.loadClass(p_name);
            if (p_resolve)
            {
                resolveClass(c);
            }
            m_classMap.put(p_name,c);
            return c;
        }
    }

    private ClassLoader m_loader;
    private final Map m_classMap = new HashMap();
}
