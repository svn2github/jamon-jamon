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

package org.jamon;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.jamon.node.Start;


public class StandardTemplateManager
    implements TemplateManager
{
    public AbstractTemplateImpl getInstance(String p_path)
        throws JamonException
    {
        return getInstance(p_path, this);
    }

    public void releaseInstance(AbstractTemplateImpl p_impl)
        throws JamonException
    {
        if (m_autoFlush)
        {
            try
            {
                p_impl.getWriter().flush();
            }
            catch (IOException e)
            {
                throw new JamonException(e);
            }
            m_cache.put(p_impl.getPath(), p_impl);
        }
    }

    public AbstractTemplateImpl getInstance(String p_path,
                                            TemplateManager p_manager)
        throws JamonException
    {
        try
        {
            initialize();

            // need to do this first to check dependencies if so enabled
            Class cls = getImplementationClass(p_path);

            AbstractTemplateImpl impl =
                (AbstractTemplateImpl) m_cache.get(p_path);
            if (impl == null)
            {
                impl = (AbstractTemplateImpl) cls
                    .getConstructor(new Class [] { TemplateManager.class,
                                                   String.class })
                    .newInstance(new Object [] { p_manager, p_path });
            }
            return impl;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JamonException(e);
        }
    }

    public void setAutoFlush(boolean p_autoFlush)
    {
        m_autoFlush = p_autoFlush;
    }

    public void setClassLoader(ClassLoader p_classLoader)
    {
        m_classLoader = p_classLoader;
        m_initialized = false;
    }

    public void setCacheSize(int p_cacheSize)
    {
        m_cacheSize = p_cacheSize;
        m_initialized = false;
    }

    public void setSourceDir(String p_templateSourceDir)
    {
        m_templateSourceDir = p_templateSourceDir;
        m_initialized = false;
    }

    public void setWorkDir(String p_workDir)
    {
        m_workDir = p_workDir;
        m_initialized = false;
    }

    public void setJavaCompiler(String p_javac)
    {
        m_javac = p_javac;
        m_javaCompiler = null;
    }

    public void setJavaCompilerNeedsRtJar(boolean p_includeRtJar)
    {
        m_includeRtJar = p_includeRtJar;
        m_javaCompiler = null;
    }

    public void setClasspath(String p_classpath)
    {
        m_classpath = p_classpath;
        m_javaCompiler = null;
    }

    public void setDynamicRecompilation(boolean p_dynamicRecompilation)
    {
        m_dynamicRecompilation = p_dynamicRecompilation;
    }

    private void logInfo(String p_message)
    {
        System.err.println(p_message);
    }

    private synchronized void initialize()
        throws IOException
    {
        if (! m_initialized)
        {
            logInfo("initializing std template mgr");
            if (m_workDir == null)
            {
                m_workDir = System.getProperty("java.io.tmpdir")
                    + "jamon"
                    + (new java.util.Random().nextInt(100000000))
                    + ".tmp";
                File workDir = new File(m_workDir);
                workDir.mkdirs();
                workDir.deleteOnExit();
            }
            m_loader = new WorkDirClassLoader(m_classLoader, m_workDir);
            m_describer =
                new TemplateDescriber(new File(m_templateSourceDir == null
                                               ? System.getProperty("user.dir")
                                               : m_templateSourceDir));
            m_cache = new LifoMultiCache(m_cacheSize);
            m_initialized = true;
        }
    }

    private String getClassName(String p_path)
    {
        return StringUtils.pathToClassName(p_path) + "Impl";
    }

    private Class loadAndResolveClass(String p_path)
        throws ClassNotFoundException
    {
        return m_loader.loadClass(getClassName(p_path));
    }

    private String getClassPath()
    {
        StringBuffer cp = new StringBuffer(m_workDir);
        if (m_classpath != null)
        {
            cp.append(PS);
            cp.append(m_classpath);
        }

        ClassLoader loader = getClass().getClassLoader();
        if (loader instanceof URLClassLoader)
        {
            URL[] urls = ((URLClassLoader)loader).getURLs();
            for (int i = 0; i < urls.length; ++i)
            {
                String url = urls[i].toExternalForm();
                if (url.startsWith("file:"))
                {
                    cp.append(PS);
                    cp.append(url.substring(5));
                }
            }
        }
        cp.append(PS);
        cp.append(System.getProperty("java.class.path"));

        if (m_includeRtJar)
        {
            cp.append(PS);
            cp.append(getRtJarPath());
        }

        logInfo("Jamon compilation CLASSPATH is " + cp);

        return cp.toString();
    }

    private String getRtJarPath()
    {
        StringBuffer path = new StringBuffer(System.getProperty("java.home"));
        path.append(FS);
        path.append("lib");
        path.append(FS);
        path.append("rt.jar");
        return path.toString();
    }

    private JavaCompiler getJavaCompiler()
    {
        if (m_javaCompiler == null)
        {
            m_javaCompiler = new JavaCompiler(m_javac, getClassPath());
        }
        return m_javaCompiler;
    }

    private Class getImplementationClass(String p_path)
        throws IOException,
               ClassNotFoundException
    {
        if (m_dynamicRecompilation)
        {
            ensureUpToDate(p_path);
            return loadAndResolveClass(p_path);
        }
        else
        {
            return m_classLoader.loadClass(getClassName(p_path));
        }
    }

    private static final String PS = System.getProperty("path.separator");
    private static final String FS = System.getProperty("file.separator");

    private boolean m_dynamicRecompilation = true;
    private TemplateDescriber m_describer;
    private String m_workDir;
    private String m_templateSourceDir;
    private String m_javac =
        System.getProperty("java.home") + FS + ".." + FS + "bin" + FS +"javac";
    private boolean m_includeRtJar = false;
    private String m_classpath = null;
    private ClassLoader m_classLoader = ClassLoader.getSystemClassLoader();
    private JavaCompiler m_javaCompiler;
    private WorkDirClassLoader m_loader;
    private boolean m_initialized;
    private boolean m_autoFlush = true;
    private LifoMultiCache m_cache;
    private int m_cacheSize = 32;

    private String prefix()
    {
        return m_workDir;
    }

    private String javaImpl(String p_path)
    {
        return prefix() + p_path + "Impl.java";
    }

    private String classImpl(String p_path)
    {
        return prefix() + p_path + "Impl.class";
    }

    private String javaIntf(String p_path)
    {
        return prefix() + p_path + ".java";
    }

    private String classIntf(String p_path)
    {
        return prefix() + p_path + ".class";
    }

    private synchronized void ensureUpToDate(String p_path)
        throws IOException
    {
        Collection seen = new HashSet();
        Collection outOfDateJavaFiles = new HashSet();
        List workQueue = new LinkedList();
        workQueue.add(p_path);

        while (!workQueue.isEmpty())
        {
            String path = (String) workQueue.remove(0);
            logInfo("processing " + path);
            seen.add(path);

            File tf = m_describer.getTemplateFile(path);
            if (!tf.exists())
            {
                logInfo(path + " source not found; assume class exists");
                continue;
            }

            File jm = new File(javaImpl(path));
            long ts = jm.lastModified();
            boolean intfGenerated = false;
            if (jm.lastModified() < tf.lastModified())
            {
                intfGenerated = generateIntfIfChanged(path);
                if (intfGenerated)
                {
                    outOfDateJavaFiles.add(javaIntf(path));
                }
                else
                {
                    // FIXME:
                    // should we really remove .java corresponding to intf?
                    // should we also remove .class(es) corresponding to intf?
                    new File(javaIntf(path)).delete();
                }
                m_dependencyCache.put
                    (path, new DependencyEntry(generateImpl(path)));
                ts = System.currentTimeMillis();
            }
            if (new File(classImpl(path)).lastModified() < ts
                || (intfGenerated
                    && new File(classIntf(path)).lastModified() < ts))
            {
                outOfDateJavaFiles.add(javaImpl(path));
            }

            DependencyEntry d = (DependencyEntry) m_dependencyCache.get(path);
            if (d == null || d.lastUpdated() < tf.lastModified())
            {
                d = new DependencyEntry(computeDependencies(path));
                m_dependencyCache.put(path, d);
            }
            for (Iterator y = d.getDependencies(); y.hasNext(); /* */)
            {
                String dp = (String) y.next();
                if (! seen.contains(dp))
                {
                    workQueue.add(dp);
                }
            }
        }

        if (!outOfDateJavaFiles.isEmpty())
        {
            purgeCache();
            compile(outOfDateJavaFiles);
            m_loader.invalidate();
        }
    }


    private void purgeCache()
    {
        m_cache = new LifoMultiCache(m_cacheSize);
    }

    private File getWriteableFile(String p_filename)
        throws IOException
    {
        File file = new File(p_filename);
        File parent = file.getParentFile();
        if (parent != null)
        {
            parent.mkdirs();
        }
        return file;
    }

    /**
     * @return dependencies
     */
    private Collection generateImpl(String p_path)
        throws IOException
    {
        logInfo("generating impl for " + p_path);

        ImplAnalyzer ia =
            new ImplAnalyzer(p_path,
                             m_describer.parseTemplate(p_path));

        File javaFile = getWriteableFile(javaImpl(p_path));
        FileWriter writer = new FileWriter(javaFile);
        try
        {
            new ImplGenerator(writer,
                              new TemplateResolver(),
                              m_describer,
                              ia)
                .generateSource();
            writer.close();
            return ia.getCalledTemplateNames();
        }
        catch (IOException e)
        {
            writer.close();
            javaFile.delete();
            throw e;
        }
    }

    private String getIntfSignatureFromClass(String p_path)
        throws IOException
    {
        logInfo("Looking for signature of "
                + StringUtils.pathToClassName(p_path));
        try
        {
            Class c = Class.forName(StringUtils.pathToClassName(p_path));
            Field f = c.getField("SIGNATURE");
            return (String) f.get(null);
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
        catch (NoSuchFieldException e)
        {
            // FIXME: this really indicates an old version ...
            return null;
        }
        catch (IllegalAccessException e)
        {
            // FIXME: this really indicates an old version ...
            return null;
        }
    }

    private boolean generateIntfIfChanged(String p_path)
        throws IOException
    {
        BaseAnalyzer bg =
            new BaseAnalyzer(m_describer.parseTemplate(p_path));

        String oldsig = getIntfSignatureFromClass(p_path);
        if (! bg.getSignature().equals(oldsig))
        {
            logInfo("generating intf for " + p_path);
            File javaFile = getWriteableFile(javaIntf(p_path));
            FileWriter writer = new FileWriter(javaFile);
            try
            {
                new IntfGenerator(new TemplateResolver(),
                                  p_path,
                                  bg,
                                  writer)
                    .generateClassSource();
                writer.close();
            }
            catch (IOException e)
            {
                writer.close();
                javaFile.delete();
                throw e;
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    private void compile(Collection p_sourceFiles)
        throws IOException
    {
        if (p_sourceFiles.isEmpty())
        {
            return;
        }

        StringBuffer buf = new StringBuffer();
        buf.append("compiling: ");
        for (Iterator i = p_sourceFiles.iterator(); i.hasNext(); /* */)
        {
            buf.append(i.next());
            if (i.hasNext())
            {
                buf.append(", ");
            }
        }
        logInfo(buf.toString());
        getJavaCompiler()
            .compile((String []) p_sourceFiles.toArray(new String [0]));
    }

    private Collection computeDependencies(String p_path)
        throws IOException
    {
        logInfo("computing dependencies for " + p_path);

        return new ImplAnalyzer(p_path,
                                m_describer.parseTemplate(p_path))
            .getCalledTemplateNames();
    }


    private Map m_dependencyCache = new HashMap();

    private static class DependencyEntry
    {
        DependencyEntry(Collection p_dependencies)
        {
            m_dependencies = p_dependencies;
            m_lastUpdated = System.currentTimeMillis();
        }

        Collection m_dependencies;
        long m_lastUpdated;

        Iterator getDependencies()
        {
            return m_dependencies.iterator();
        }

        long lastUpdated()
        {
            return m_lastUpdated;
        }
    }

}
