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

import java.io.File;
import java.io.FilenameFilter;
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
 * An implementation of the {@link TemplateManager} interface which
 * supports dynamic regeneration and recompilation of templates as
 * they are changed, much as JSP does.
 *
 * <code>RecompilingTemplateManager</code> instances are thread-safe.  In
 * your applications, you generally want exactly one instance of a
 * RecompilingTemplateManager (i.e. a singleton).
 *
 * Configuration of a <code>RecompilingTemplateManager</code> occurs only
 * at construction time, and is determined by the
 * <code>RecompilingTemplateManager.Data</code> object passed to the
 * constructor. The properties on the <code>Data</code> are:

 * <ul>

 *   <li><b>setSourceDir</b> - determines where the
 *   <code>RecompilingTemplateManager</code> looks for template source
 *   files. Default is the current directory, which is most likely
 *   unsuitable for most situations.

 *   <li><b>setWorkDir</b> - determines where the generated Java
 *   source files corresponding to templates are placed. Default is
 *   uniquely generated subdirectory under the directory specified by
 *   the system property <tt>java.io.tmpdir</tt>.

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

 *   <li><b>setClassLoader</b> - used to set the class loader
 *   explicitly. Default is use the class loader of the
 *   <code>RecompilingTemplateManager</code> instance.

 * </ul>
 */

public class RecompilingTemplateManager
    implements TemplateManager
{
    public static class Data
    {
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
    }

    public RecompilingTemplateManager()
    {
        this(new Data());
    }

    private static String getDefaultJavac()
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
                        bindir)
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

    public RecompilingTemplateManager(Data p_data)
    {
        m_classLoader = p_data.classLoader == null
            ? getClass().getClassLoader()
            : p_data.classLoader;

        m_workDir = p_data.workDir == null
            ? getDefaultWorkDir()
            : p_data.workDir;
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

    public AbstractTemplateProxy.Intf constructImpl(
        AbstractTemplateProxy p_proxy)
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
     **/
    public AbstractTemplateProxy.Intf constructImpl(
        AbstractTemplateProxy p_proxy, TemplateManager p_manager)
    {
        return p_proxy.constructImpl(getImplClass(p_proxy.getClass()),
                                     p_manager);
    }

    /**
     * Given a template path, return a proxy for that template.
     *
     * @param p_path the path to the template
     *
     * @return a <code>Template</code> proxy instance
     **/
    public AbstractTemplateProxy constructProxy(String p_path)
    {
        try
        {
            // need to do this first to check dependencies if so enabled
            return (AbstractTemplateProxy) getProxyClass(p_path)
                .getConstructor(new Class [] { TemplateManager.class })
                .newInstance(new Object [] { this });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JamonRuntimeException(e);
        }
    }

    private static void trace(String p_message)
    {
        System.err.println(p_message);
    }

    private static String getDefaultWorkDir()
    {
        File workDir =
            new File(System.getProperty("java.io.tmpdir"),
                     "jamon"
                     + (new java.util.Random().nextInt(100000000))
                     + ".tmp");
        if (! workDir.mkdirs())
        {
            throw new JamonRuntimeException
                ("Unable to create default work directory " + workDir);
        }
        workDir.deleteOnExit();
        return workDir.toString();
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
    {
        return getTemplateClass(StringUtils.classToTemplatePath(p_proxyClass),
                                p_proxyClass.getName() + "Impl");
    }

    private Class getProxyClass(String p_path)
    {
        return getTemplateClass(p_path,
                                StringUtils.templatePathToClassName(p_path));
    }

    private Class getTemplateClass(String p_path, String p_className)
    {
        try
        {
            try
            {
                ensureUpToDate(p_path,
                               new TemplateDescriber(m_templateSource,
                                                     m_loader));
                return m_loader.loadClass(p_className);
            }
            catch (ClassNotFoundException e)
            {
                if (! m_templateSource.available(p_path))
                {
                    throw new UnknownTemplateException(p_path);
                }
                else
                {
                    throw new JamonRuntimeException(e);
                }
            }
        }
        catch (IOException e)
        {
            throw new JamonRuntimeException(e);
        }
    }

    private final TemplateSource m_templateSource;
    private final String m_workDir;
    private final ClassLoader m_classLoader;
    private final JavaCompiler m_javaCompiler;
    private final WorkDirClassLoader m_loader;

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

            boolean intfGenerated = false;
            long modTime = m_templateSource.lastModified(path);

            String intfFileName = javaIntf(path);
            File intfFile = new File(intfFileName);
            if (intfFile.lastModified() < modTime)
            {
                TemplateUnit templateUnit =
                    new Analyzer(path, p_describer).analyze();
                String signature = templateUnit.getSignature();
                if (isIntfChanged(path, signature, m_classLoader))
                {
                    if (isIntfChanged(path, signature, m_loader))
                    {
                        generateIntf(path, p_describer, templateUnit);
                        outOfDateJavaFiles.add(intfFileName);
                        intfGenerated = true;
                    }
                }
                else
                {
                    intfFile.delete();
                    deleteClassFilesFor(path);
                }

            }

            File jm = new File(javaImpl(path));
            long ts = jm.lastModified();
            if (jm.lastModified() < modTime)
            {
                m_dependencyCache.put
                    (path,
                     new DependencyEntry(generateImpl(path,p_describer)));
                ts = System.currentTimeMillis();
            }

            if (new File(classImpl(path)).lastModified() < ts || intfGenerated)
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
            String errors = compile(outOfDateJavaFiles);
            m_loader.invalidate();
            if (errors != null)
            {
                throw new TemplateCompilationException(errors);
            }
        }
    }

    private void deleteClassFilesFor(String p_path)
    {
        int i = p_path.lastIndexOf('/');
        String templateName = i < 0 ? p_path : p_path.substring(i+1);
        File dir = new File(new File(m_workDir),
                            StringUtils.templatePathToFileDir(p_path));
        String[] files = dir.list();
        if (files != null)
        {
            for (int j = 0; j < files.length; ++j)
            {
                if (StringUtils.isGeneratedClassFilename(templateName,
                                                         files[j]))
                {
                    new File(dir, files[j]).delete();
                    // FIXME: error checking?
                }
            }
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

    private String getIntfSignatureFromClass(String p_path,
                                             ClassLoader p_loader)
    {
        if (TRACE)
        {
            trace("Looking for signature of "
                    + StringUtils.templatePathToClassName(p_path));
        }
        try
        {
            return (String)
                p_loader.loadClass(StringUtils.templatePathToClassName(p_path))
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

    private boolean isIntfChanged(String p_path,
                                  String p_signature,
                                  ClassLoader p_loader)
    {
        return ! p_signature.equals(getIntfSignatureFromClass(p_path,
                                                              p_loader));
    }

    private void generateIntf(String p_path,
                                 TemplateDescriber p_describer,
                                 TemplateUnit p_templateUnit)
        throws IOException
    {
        if (TRACE)
        {
            trace("generating intf for " + p_path);
        }
        File javaFile = getWriteableFile(javaIntf(p_path));
        FileWriter writer = new FileWriter(javaFile);
        try
        {
            new ProxyGenerator(writer, p_describer, p_templateUnit)
                .generateClassSource();
            writer.close();
        }
        catch (IOException e)
        {
            writer.close();
            javaFile.delete();
            throw e;
        }
    }

    private String compile(Collection p_sourceFiles)
        throws IOException
    {
        if (p_sourceFiles.isEmpty())
        {
            return null;
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
        return m_javaCompiler
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

    private static final boolean TRACE =
        Boolean.valueOf(System.getProperty
                        (RecompilingTemplateManager.class.getName()
                         + ".trace" )).booleanValue();
}
