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
     * @exception JamonException if something goes wrong
     */
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
            impl.escaping(m_escaping);
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

    /**
     * Set whether templates automatically flush the writer after
     * rendering. Default is true.
     *
     * @param p_autoFlush whether template instances should flush automatically
     *
     * @return this
     */

    public StandardTemplateManager setAutoFlush(boolean p_autoFlush)
    {
        m_autoFlush = p_autoFlush;
        return this;
    }

    /**
     * Set default escaping. Default is null.
     *
     * @param p_escaping the default escaping
     *
     * @return this
     */

    public StandardTemplateManager setDefaultEscaping(Escaping p_escaping)
    {
        m_escaping = p_escaping;
        return this;
    }

    /**
     * Set the parent class loader for template instances. Default is
     * use the class laoder of the
     * <code>StandardTemplateManager</code> instance.
     *
     * @param p_classLoader the <code>ClassLoader</code> to use.
     *
     * @return this
     */

    public StandardTemplateManager setClassLoader(ClassLoader p_classLoader)
    {
        m_classLoader = p_classLoader;
        m_initialized = false;
        return this;
    }

    /**
     * Set the maximum number of * template instances cached. Default
     * is 50.
     *
     * @param p_cacheSize the cache size
     *
     * @return this
     */

    public StandardTemplateManager setCacheSize(int p_cacheSize)
    {
        m_cacheSize = p_cacheSize;
        m_initialized = false;
        return this;
    }

    /**
     * Determines where to look for template source
     * files. Default is the current directory, which is most likely
     * unsuitable for most situations.
     *
     * @param p_templateSourceDir where to look for template sources
     *
     * @return this
     */
    public StandardTemplateManager setSourceDir(String p_templateSourceDir)
    {
        m_templateSourceDir = p_templateSourceDir;
        m_initialized = false;
        return this;
    }

    /**
     * Set where the generated Java source files corresponding to
     * templates are placed. Default is uniquely generated subdirectory
     * under the directory specified by the system property
     * <tt>java.io.tmpdir</tt>.
     *
     * @param p_workDir where to place generated java files
     *
     * @return this
     */
    public StandardTemplateManager setWorkDir(String p_workDir)
    {
        m_workDir = p_workDir;
        m_initialized = false;
        return this;
    }

    /**
     *  Set what program to execute to compile the generated Java
     *  source files. Default is <tt>bin/javac</tt> under the
     *  directory specified by the system property <tt>java.home</tt>.
     *
     * @param p_javac the java compiler program path
     *
     * @return this
     */
    public StandardTemplateManager setJavaCompiler(String p_javac)
    {
        m_javac = p_javac;
        m_javaCompiler = null;
        return this;
    }

    /**
     * Set whether rt.jar needs to be explicitly supplied in the
     * classpath when compiling generated Java source files; this is
     * useful to enable compilation via <code>Jikes</code>. Default is
     * false.
     *
     * @param p_includertJar whether to include <code>rt.jar</code> in
     * the classpath supplied to the java compiler
     *
     * @return this
     */
    public StandardTemplateManager setJavaCompilerNeedsRtJar(boolean p_includeRtJar)
    {
        m_includeRtJar = p_includeRtJar;
        m_javaCompiler = null;
        return this;
    }

    /**
     * Specify additional components to prepend to the classpath when
     * compiling generated Java source files. Default is null.
     *
     * @param p_classpath classpath components prepended to the
     * classpath supplied to the java compiler
     *
     * @return this
     */
    public StandardTemplateManager setClasspath(String p_classpath)
    {
        m_classpath = p_classpath;
        m_javaCompiler = null;
        return this;
    }
    /**
     * Determines whether classes
     * corresponding to templates should be dynamically recompiled as
     * necessary. Default is true; set to false for production.
     *
     * @param p_dynamicRecompilation whether to dynamically regenerate
     * and recompile changed templates
     *
     * @return this
     */

    public StandardTemplateManager setDynamicRecompilation(boolean p_dynamicRecompilation)
    {
        m_dynamicRecompilation = p_dynamicRecompilation;
        return this;
    }

    private void trace(String p_message)
    {
        System.err.println(p_message);
    }

    private synchronized void initialize()
        throws IOException
    {
        if (! m_initialized)
        {
            if (TRACE)
            {
                trace("initializing std template mgr");
            }
            if (m_workDir == null)
            {
                File workDir =
                    new File(System.getProperty("java.io.tmpdir"),
                             "jamon"
                             + (new java.util.Random().nextInt(100000000))
                             + ".tmp");
                m_workDir = workDir.getCanonicalPath();
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
        return StringUtils.templatePathToClassName(p_path) + "Impl";
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
            cp.append(File.pathSeparator);
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
                    cp.append(File.pathSeparator);
                    cp.append(url.substring(5));
                }
            }
        }
        cp.append(File.pathSeparator);
        cp.append(System.getProperty("sun.boot.class.path"));
        cp.append(File.pathSeparator);
        cp.append(System.getProperty("java.class.path"));

        if (m_includeRtJar)
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

    private String getRtJarPath()
    {
        StringBuffer path = new StringBuffer(System.getProperty("java.home"));
        path.append(File.separator);
        path.append("lib");
        path.append(File.separator);
        path.append("rt.jar");
        return path.toString();
    }

    private JavaCompiler getJavaCompiler()
        throws IOException
    {
        if (m_javaCompiler == null)
        {
            if (m_javac == null)
            {
                m_javac = getDefaultJavac();
            }
            m_javaCompiler = new JavaCompiler(m_javac, getClassPath());
        }
        return m_javaCompiler;
    }

    private String getDefaultJavac()
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

    private boolean m_dynamicRecompilation = true;
    private TemplateDescriber m_describer;
    private Escaping m_escaping = Escaping.DEFAULT;
    private String m_workDir;
    private String m_templateSourceDir;
    private String m_javac;
    private boolean m_includeRtJar = false;
    private String m_classpath = null;
    private ClassLoader m_classLoader = getClass().getClassLoader();
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
        getJavaCompiler()
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
