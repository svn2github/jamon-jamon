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

package org.jamon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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

/**
 * The standard implementation of the @{link TemplateManager}
 * interface.  The <code>StandardTemplateManager</code> actually
 * functions in both development and production environments,
 * depending on how it is configured. In its default configuration,
 * the <code>StandardTemplateManager</code> supports dynamic
 * regeneration and recompilation of templates as they are changed,
 * much as JSP does.
 *
 * The properties which control the behavior are:
 * <ul>

 *   <li>{@link #setSourceDir} - determines where the
 *   <code>StandardTemplateManager</code> looks for template source
 *   files. Default is the current directory, which is most likely
 *   unsuitable for most situations.

 *   <li>{@link #setWorkDir} - determines where the generated Java
 *   source files corresponding to templates are placed. Default is
 *   uniquely generated subdirectory under the directory specified by
 *   the system property <tt>java.io.tmpdir</tt>.

 *   <li>{@link #setDynamicRecompilation} - determines whether classes
 *   corresponding to templates should be dynamically recompiled as
 *   necessary. Default is true; set to false for production.

 *   <li>{@link #setCacheSize} - used to set the maximum number of
 *   template instances cached. Default is 50.

 *   <li>{@link #setJavaCompiler} - determines what program to execute
 *   to compile the generated Java source files. Default is
 *   <tt>bin/javac</tt> under the directory specified by the system
 *   property <tt>java.home</tt>.

 *   <li>{@link #setJavaCompilerNeedsRtJar} - determines whether rt.jar
 *   needs to be explicitly supplied in the classpath when compiling
 *   generated Java source files; this is useful to enable compilation
 *   via <code>Jikes</code>. Default is false.

 *   <li>{@link #setClasspath} - used to specify additional components
 *   to prepend to the classpath when compiling generated Java source
 *   files. Default is null.

 *   <li>{@link #setAutoFlush} - determines whether templates
 *   automatically flush the writer after rendering. Default is true.

 *   <li>{@link #setClassLoader} - used to set the class loader
 *   explicitly. Default is use the class laoder of the
 *   <code>StandardTemplateManager</code> instance.

 *   <li>{@link #setDefaultEscaping} - used to set the default escaping
 *   Default is null.

 * </ul>
 */

public class StandardTemplateManager
    implements TemplateManager
{
    public static class Data
    {
        public Data setAutoFlush(boolean p_autoFlush)
        {
            autoFlush = p_autoFlush;
            return this;
        }
        private boolean autoFlush = true;

        public Data setSourceDir(String p_sourceDir)
        {
            sourceDir = p_sourceDir;
            return this;
        }
        private String sourceDir;

        public Data setWorkDir(String p_workDir)
        {
            workDir = p_workDir;
            return this;
        }
        private String workDir;

        public Data setDynamicRecompilation(boolean p_dynamicRecompilation)
        {
            dynamicRecompilation = p_dynamicRecompilation;
            return this;
        }
        private boolean dynamicRecompilation = true;

        public Data setCacheSize(int p_cacheSize)
        {
            cacheSize = p_cacheSize;
            return this;
        }
        private int cacheSize = 50;

        public Data setJavaCompiler(String p_javaCompiler)
        {
            javaCompiler = p_javaCompiler;
            return this;
        }
        private String javaCompiler;

        public Data setJavaCompilerNeedsRtJar(boolean p_javaCompilerNeedsRtJar)
        {
            javaCompilerNeedsRtJar = p_javaCompilerNeedsRtJar;
            return this;
        }
        private boolean javaCompilerNeedsRtJar;

        public Data setClasspath(String p_classpath)
        {
            classpath = p_classpath;
            return this;
        }
        private String classpath;

        public Data setClassLoader(ClassLoader p_classLoader)
        {
            classLoader = p_classLoader;
            return this;
        }
        private ClassLoader classLoader = getClass().getClassLoader();

        public Data setDefaultEscaping(Escaping p_escaping)
        {
            escaping = p_escaping;
            return this;
        }
        private Escaping escaping = Escaping.DEFAULT;
    }

    public StandardTemplateManager()
        throws IOException
    {
        this(new Data());
    }

    public StandardTemplateManager(Data p_data)
        throws IOException
    {
        m_autoFlush = p_data.autoFlush;
        m_escaping = p_data.escaping == null
            ? Escaping.DEFAULT
            : p_data.escaping;
        m_classLoader = p_data.classLoader == null
            ? getClass().getClassLoader()
            : p_data.classLoader;
        m_dynamicRecompilation = p_data.dynamicRecompilation;
        m_cache = new LifoMultiCache(p_data.cacheSize);
        if (m_dynamicRecompilation)
        {
            m_workDir = p_data.workDir == null
                ? getDefaultWorkDir()
                : p_data.workDir;
            m_javaCompiler =
                new JavaCompiler(p_data.javaCompiler == null
                                 ? getDefaultJavac()
                                 : p_data.javaCompiler,
                                 getClassPath(m_workDir,
                                              p_data.classpath,
                                              p_data.javaCompilerNeedsRtJar,
                                              m_classLoader));
            m_describer =
                new TemplateDescriber(new File(p_data.sourceDir == null
                                               ? System.getProperty("user.dir")
                                               : p_data.sourceDir));
            m_loader = new WorkDirClassLoader(m_classLoader, m_workDir);
        }
        else
        {
            m_describer = null;
            m_javaCompiler = null;
            m_workDir = null;
            m_loader = null;
        }
    }

    public AbstractTemplateImpl getInstance(String p_path)
        throws IOException
    {
        return getInstance(p_path, this);
    }

    public void releaseInstance(AbstractTemplateImpl p_impl)
        throws IOException
    {
        if (m_autoFlush)
        {
            Writer writer = p_impl.getWriter();
            if (writer != null)
            {
                writer.flush();
            }
            m_cache.put(p_impl.getPath(), p_impl);
        }
    }

    /**
     * Provided for subclasses and composing classes. Given a template
     * path, return an appropriate instance which corresponds to the
     * executable code for that template.
     *
     * @param p_path the path to the template
     * @param p_manager the {@link TemplateManager} to supply to the
     * template
     *
     * @return a <code>Template</code> instance
     *
     * @exception IOException if something goes wrong
     */
    public AbstractTemplateImpl getInstance(String p_path,
                                            TemplateManager p_manager)
        throws IOException
    {
        try
        {
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
                impl.escaping(m_escaping);
            }
            return impl;
        }
        catch (IOException e)
        {
            throw e;
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

    private static void trace(String p_message)
    {
        System.err.println(p_message);
    }

    private String getClassName(String p_path)
    {
        return StringUtils.templatePathToClassName(p_path) + "Impl";
    }

    private Class loadAndResolveClass(String p_path)
        throws ClassNotFoundException
    {
        return m_loader.loadClass(getClassName(p_path));
    }

    private static String getDefaultWorkDir()
        throws IOException
    {
        File workDir =
            new File(System.getProperty("java.io.tmpdir"),
                     "jamon"
                     + (new java.util.Random().nextInt(100000000))
                     + ".tmp");
        workDir.mkdirs();
        workDir.deleteOnExit();
        return workDir.getCanonicalPath();
    }

    private static String getClassPath(String p_start,
                                       String p_classpath,
                                       boolean p_includeRtJar,
                                       ClassLoader p_classLoader)
    {
        StringBuffer cp = new StringBuffer(p_start);
        if (p_classpath != null)
        {
            cp.append(File.pathSeparator);
            cp.append(p_classpath);
        }

        ClassLoader loader = p_classLoader;
        if (loader instanceof URLClassLoader)
        {
            URL[] urls = ((URLClassLoader)loader).getURLs();
            for (int i = 0; i < urls.length; ++i)
            {
                String url = urls[i].toExternalForm();
                if (url.startsWith("file:"))
                {
                    cp.append(File.pathSeparator);
                    cp.append(url.substring(5));
                }
            }
        }
        cp.append(File.pathSeparator);
        cp.append(System.getProperty("sun.boot.class.path"));
        cp.append(File.pathSeparator);
        cp.append(System.getProperty("java.class.path"));

        if (p_includeRtJar)
        {
            cp.append(File.pathSeparator);
            cp.append(getRtJarPath());
        }

        if (TRACE)
        {
            trace("Jamon compilation CLASSPATH is " + cp);
        }

        return cp.toString();
    }

    private static String getRtJarPath()
    {
        StringBuffer path = new StringBuffer(System.getProperty("java.home"));
        path.append(File.separator);
        path.append("lib");
        path.append(File.separator);
        path.append("rt.jar");
        return path.toString();
    }

    private static String getDefaultJavac()
        throws IOException
    {
        // FIXME: does this work on windows?
        // FIXME: should we just use the javac in the default path?
        String bindir;
        if( "Mac OS X".equals( System.getProperty( "os.name" ) ) )
        {
            bindir = "Commands";
        }
        else
        {
            bindir = "bin";
        }
        return new File(new File(System.getProperty("java.home")).getParent(),
                        bindir).getCanonicalPath()
            + File.separator
            + "javac";
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

    protected ClassLoader getWorkClassLoader()
        throws IOException
    {
        return m_loader;
    }

    private final boolean m_dynamicRecompilation;
    private final TemplateDescriber m_describer;
    private final Escaping m_escaping;
    private final String m_workDir;
    private final ClassLoader m_classLoader;
    private final JavaCompiler m_javaCompiler;
    private final WorkDirClassLoader m_loader;
    private final boolean m_autoFlush;
    private final LifoMultiCache m_cache;

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
            if (TRACE)
            {
                trace("processing " + path);
            }
            seen.add(path);

            File tf = m_describer.getTemplateFile(path);
            if (!tf.exists())
            {
                if (TRACE)
                {
                    trace(path + " source not found; assume class exists");
                }
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
        m_cache.clear();
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
        if (TRACE)
        {
            trace("generating impl for " + p_path);
        }

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
        if (TRACE)
        {
            trace("Looking for signature of "
                    + StringUtils.templatePathToClassName(p_path));
        }
        try
        {
            return (String)
                Class.forName(StringUtils.templatePathToClassName(p_path))
                    .getField("SIGNATURE")
                    .get(null);
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
            if (TRACE)
            {
                trace("generating intf for " + p_path);
            }
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
        if (TRACE)
        {
            trace(buf.toString());
        }
        m_javaCompiler
            .compile((String []) p_sourceFiles.toArray(new String [0]));
    }

    private Collection computeDependencies(String p_path)
        throws IOException
    {
        if (TRACE)
        {
            trace("computing dependencies for " + p_path);
        }

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

    private static final boolean TRACE =
        Boolean.valueOf(System.getProperty
                        (StandardTemplateManager.class.getName()
                         + ".trace" )).booleanValue();
}
