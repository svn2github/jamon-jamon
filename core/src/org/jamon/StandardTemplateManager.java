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

import org.jamon.escaping.Escaping;
import org.jamon.util.JavaCompiler;
import org.jamon.util.ExternalJavaCompiler;
import org.jamon.util.InternalJavaCompiler;
import org.jamon.util.StringUtils;
import org.jamon.util.WorkDirClassLoader;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.Analyzer;
import org.jamon.codegen.ImplGenerator;
import org.jamon.codegen.ProxyGenerator;
import org.jamon.codegen.TemplateUnit;

/**
 * The standard implementation of the {@link TemplateManager}
 * interface.  The <code>StandardTemplateManager</code> actually
 * functions in both development and production environments,
 * depending on how it is configured. In its default configuration,
 * the <code>StandardTemplateManager</code> supports dynamic
 * regeneration and recompilation of templates as they are changed,
 * much as JSP does.
 *
 * <code>StandardTemplateManager</code> instances are thread-safe.  In
 * your applications, you generally want exactly one instance of a
 * StandardTemplateManager (i.e. a singleton).
 *
 * Configuration of a <code>StandardTemplateManager</code> occurs only
 * at construction time, and is determined by the
 * <code>StandardTemplateManager.Data</code> object passed to the
 * constructor. The properties on the <code>Data</code> are:

 * <ul>

 *   <li><b>setSourceDir</b> - determines where the
 *   <code>StandardTemplateManager</code> looks for template source
 *   files. Default is the current directory, which is most likely
 *   unsuitable for most situations.

 *   <li><b>setWorkDir</b> - determines where the generated Java
 *   source files corresponding to templates are placed. Default is
 *   uniquely generated subdirectory under the directory specified by
 *   the system property <tt>java.io.tmpdir</tt>.

 *   <li><b>setDynamicRecompilation</b> - determines whether classes
 *   corresponding to templates should be dynamically recompiled as
 *   necessary. Default is true; set to false for production.

 *   <li><b>setJavaCompiler</b> - determines what program to execute
 *   to compile the generated Java source files. Default is
 *   <tt>bin/javac</tt> under the directory specified by the system
 *   property <tt>java.home</tt>.

 *   <li><b>setJavaCompilerNeedsRtJar</b> - determines whether rt.jar
 *   needs to be explicitly supplied in the classpath when compiling
 *   generated Java source files; this is useful to enable compilation
 *   via <code>Jikes</code>. Default is false.

 *   <li><b>setClasspath</b> - used to specify additional components
 *   to prepend to the classpath when compiling generated Java source
 *   files. Default is null.

 *   <li><b>setAutoFlush</b> - determines whether templates
 *   automatically flush the writer after rendering. Default is true.

 *   <li><b>setClassLoader</b> - used to set the class loader
 *   explicitly. Default is use the class laoder of the
 *   <code>StandardTemplateManager</code> instance.

 *   <li><b>setDefaultEscaping</b> - used to set the default escaping
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

        public Data setTemplateSource(TemplateSource p_templateSource)
        {
            templateSource = p_templateSource;
            return this;
        }
        private TemplateSource templateSource;

        public Data setWorkDir(String p_workDir)
        {
            workDir = p_workDir;
            return this;
        }
        private String workDir;

        public Data setCleanWorkDir(boolean p_cleanWorkDir)
        {
            cleanWorkDir = p_cleanWorkDir;
            return this;
        }
        private boolean cleanWorkDir = true;

        public Data setDynamicRecompilation(boolean p_dynamicRecompilation)
        {
            dynamicRecompilation = p_dynamicRecompilation;
            return this;
        }
        private boolean dynamicRecompilation = true;

        public Data setJavaCompiler(String p_javaCompiler)
        {
            javaCompiler = p_javaCompiler;
            return this;
        }
        private String javaCompiler;

        public Data setJavaCompilerNeedsRtJar(boolean p_javaCompilerNeedsRtJar)
        {
            javaCompilerNeedsRtJar = new Boolean(p_javaCompilerNeedsRtJar);
            return this;
        }
        private Boolean javaCompilerNeedsRtJar;

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

    private static JavaCompiler getInternalJavaCompiler(String p_classpath)
        throws Exception
    {
        return new InternalJavaCompiler(p_classpath);
    }

    private static JavaCompiler makeCompiler(Data p_data,
                                             String p_workDir,
                                             ClassLoader p_classLoader)
        throws IOException
    {
        String javac = p_data.javaCompiler;
        if (javac == null)
        {
            try
            {
                return getInternalJavaCompiler(getClasspath(p_workDir,
                                                            p_data.classpath,
                                                            false,
                                                            p_classLoader));
            }
            catch (Exception e)
            {
                // well, we tried
                javac = getDefaultJavac();
            }
        }
        return new ExternalJavaCompiler
            (javac,
             getClasspath(p_workDir,
                          p_data.classpath,
                          p_data.javaCompilerNeedsRtJar == null
                          ? javac.endsWith("jikes")
                          : p_data.javaCompilerNeedsRtJar.booleanValue(),
                          p_classLoader));
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
        if (m_dynamicRecompilation)
        {
            m_workDir = p_data.workDir == null
                ? getDefaultWorkDir()
                : p_data.workDir;
            if (p_data.cleanWorkDir)
            {
                fullDelete(new File(m_workDir));
            }
            m_javaCompiler = makeCompiler(p_data, m_workDir, m_classLoader);

            if (p_data.templateSource != null)
            {
                m_templateSource = p_data.templateSource;
            }
            else
            {
                m_templateSource =
                    new FileTemplateSource(p_data.sourceDir == null
                                           ? System.getProperty("user.dir")
                                           : p_data.sourceDir);
            }

            m_loader = new WorkDirClassLoader(m_classLoader, m_workDir);
        }
        else
        {
            m_templateSource = null;
            m_javaCompiler = null;
            m_workDir = null;
            m_loader = null;
        }
    }

    public AbstractTemplateProxy.Intf constructImpl(
        AbstractTemplateProxy p_proxy)
        throws IOException
    {
        return constructImpl(p_proxy, this);
    }

    /**
     * Provided for subclasses and composing classes. Given a template
     * proxy path, return an instance of the executable code for that
     * proxy's template.
     *
     * @param p_proxy a proxy for the template
     * @param p_manager the {@link TemplateManager} to supply to the
     * template
     *
     * @return a <code>Template</code> instance
     *
     * @exception IOException if something goes wrong
     **/
    public AbstractTemplateProxy.Intf constructImpl(
        AbstractTemplateProxy p_proxy, TemplateManager p_manager)
        throws IOException
    {
        if (m_dynamicRecompilation)
        {
            return p_proxy.constructImpl(getImplClass(p_proxy.getClass()),
                                         p_manager);
        }
        else
        {
            return p_proxy.constructImpl(p_manager);
        }
    }

    /**
     * Given a template path, return a proxy for that template.
     *
     * @param p_path the path to the template
     *
     * @return a <code>Template</code> proxy instance
     *
     * @exception IOException if something goes wrong
     **/
    public AbstractTemplateProxy constructProxy(String p_path)
        throws IOException
    {
        try
        {
            // need to do this first to check dependencies if so enabled
            return (AbstractTemplateProxy) getProxyClass(p_path)
                .getConstructor(new Class [] { TemplateManager.class })
                .newInstance(new Object [] { this });
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

    private static String getDefaultWorkDir()
        throws IOException
    {
        File workDir =
            new File(System.getProperty("java.io.tmpdir"),
                     "jamon"
                     + (new java.util.Random().nextInt(100000000))
                     + ".tmp");
        if (! workDir.mkdirs())
        {
            throw new JamonException("Unable to create default work directory "
                                     + workDir);
        }
        workDir.deleteOnExit();
        return workDir.getCanonicalPath();
    }


    private static void extractClasspath(ClassLoader p_classLoader,
                                         StringBuffer p_classpath)
    {
        if (p_classLoader instanceof URLClassLoader)
        {
            URL[] urls = ((URLClassLoader)p_classLoader).getURLs();
            for (int i = 0; i < urls.length; ++i)
            {
                String url = urls[i].toExternalForm();
                if (url.startsWith("file:"))
                {
                    p_classpath.append(File.pathSeparator);
                    p_classpath.append(url.substring(5));
                }
            }
        }
        if (p_classLoader.getParent() != null)
        {
            extractClasspath(p_classLoader.getParent(), p_classpath);
        }
    }

    private static String getClasspath(String p_start,
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

        extractClasspath(p_classLoader, cp);
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

    private Class getImplClass(Class p_proxyClass)
        throws IOException
    {
        return getTemplateClass(StringUtils.classToTemplatePath(p_proxyClass),
                                p_proxyClass.getName() + "Impl");
    }

    private Class getProxyClass(String p_path)
        throws IOException
    {
        return getTemplateClass(p_path,
                                StringUtils.templatePathToClassName(p_path));
    }

    private Class getTemplateClass(String p_path, String p_className)
        throws IOException
    {
        try
        {
            if (m_dynamicRecompilation)
            {
                ensureUpToDate(p_path,
                               new TemplateDescriber(m_templateSource,
                                                     m_loader));
                return m_loader.loadClass(p_className);
            }
            else
            {
                return m_classLoader.loadClass(p_className);
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new JamonException(e);
        }
    }

    private final TemplateSource m_templateSource;
    private final boolean m_dynamicRecompilation;
    private final Escaping m_escaping;
    private final String m_workDir;
    private final ClassLoader m_classLoader;
    private final JavaCompiler m_javaCompiler;
    private final WorkDirClassLoader m_loader;
    private final boolean m_autoFlush;

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

    private synchronized void ensureUpToDate(String p_path,
                                             TemplateDescriber p_describer)
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

            if (! m_templateSource.available(path))
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
            long modTime = m_templateSource.lastModified(path);
            if (jm.lastModified() < modTime)
            {
                intfGenerated = generateIntfIfChanged(path, p_describer);
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
                    (path,
                     new DependencyEntry(generateImpl(path,p_describer)));
                ts = System.currentTimeMillis();
            }
            if (new File(classImpl(path)).lastModified() < ts
                || (intfGenerated
                    && new File(classIntf(path)).lastModified() < ts))
            {
                outOfDateJavaFiles.add(javaImpl(path));
            }

            DependencyEntry d = (DependencyEntry) m_dependencyCache.get(path);
            if (d == null || d.lastUpdated() < modTime)
            {
                d = new DependencyEntry(computeDependencies(path,p_describer));
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
            compile(outOfDateJavaFiles);
            m_loader.invalidate();
        }
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
    private Collection generateImpl(String p_path,
                                    TemplateDescriber p_describer)
        throws IOException
    {
        if (TRACE)
        {
            trace("generating impl for " + p_path);
        }

        TemplateUnit templateUnit = new Analyzer(p_path,p_describer).analyze();

        File javaFile = getWriteableFile(javaImpl(p_path));
        FileWriter writer = new FileWriter(javaFile);
        try
        {
            new ImplGenerator(writer, p_describer, templateUnit)
                .generateSource();
            writer.close();
            return templateUnit.getTemplateDependencies();
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

    private boolean generateIntfIfChanged(String p_path,
                                          TemplateDescriber p_describer)
        throws IOException
    {
        TemplateUnit templateUnit = new Analyzer(p_path, p_describer)
            .analyze();

        String oldsig = getIntfSignatureFromClass(p_path);
        if (! templateUnit.getSignature().equals(oldsig))
        {
            if (TRACE)
            {
                trace("generating intf for " + p_path);
            }
            File javaFile = getWriteableFile(javaIntf(p_path));
            FileWriter writer = new FileWriter(javaFile);
            try
            {
                new ProxyGenerator(writer, p_describer, templateUnit)
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

    private Collection computeDependencies(String p_path,
                                           TemplateDescriber p_describer)
        throws IOException
    {
        if (TRACE)
        {
            trace("computing dependencies for " + p_path);
        }

        return new Analyzer(p_path, p_describer)
            .analyze()
            .getTemplateDependencies();
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


    private static void fullDelete( File p_directory )
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

    private static final boolean TRACE =
        Boolean.valueOf(System.getProperty
                        (StandardTemplateManager.class.getName()
                         + ".trace" )).booleanValue();
}
