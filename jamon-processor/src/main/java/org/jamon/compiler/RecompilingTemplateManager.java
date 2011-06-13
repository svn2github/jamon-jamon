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
 * Contributor(s): Ian Robertson, Matt Raible
 */

package org.jamon.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jamon.AbstractTemplateImpl;
import org.jamon.AbstractTemplateManager;
import org.jamon.AbstractTemplateProxy;
import org.jamon.IdentityTemplateReplacer;
import org.jamon.TemplateManager;
import org.jamon.TemplateReplacer;
import org.jamon.annotations.Template;
import org.jamon.api.SourceGenerator;
import org.jamon.api.TemplateSource;
import org.jamon.codegen.Analyzer;
import org.jamon.codegen.ImplGenerator;
import org.jamon.codegen.ProxyGenerator;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateUnit;
import org.jamon.util.JavaCompiler;
import org.jamon.util.JavaCompilerFactory;
import org.jamon.util.StringUtils;
import org.jamon.util.WorkDirClassLoader;

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
 *   <tt>bin/javac</tt> (<tt>Commands/javac</tt> on MacOS) under the directory
 *   specified by the system property <tt>java.home</tt>.

 *   <li><b>setClasspath</b> - used to specify additional components
 *   to prepend to the classpath when compiling generated Java source
 *   files. Default is null.

 *   <li><b>setClassLoader</b> - used to set the class loader
 *   explicitly. Default is use the class loader of the
 *   <code>RecompilingTemplateManager</code> instance.

 * </ul>
 */

public class RecompilingTemplateManager extends AbstractTemplateManager
{
    public static class Data
    {
        public Data setSourceDir(String p_sourceDir)
        {
            sourceDir = p_sourceDir;
            return this;
        }
        public String getSourceDir()
        {
            return sourceDir;
        }
        private String sourceDir;

        public Data setTemplateSource(TemplateSource p_templateSource)
        {
            templateSource = p_templateSource;
            return this;
        }
        public TemplateSource getTemplateSource()
        {
            return templateSource;
        }
        private TemplateSource templateSource;

        public Data setWorkDir(String p_workDir)
        {
            workDir = p_workDir;
            return this;
        }
        public String getWorkDir()
        {
            return workDir;
        }
        private String workDir;

        public Data setJavaCompiler(String p_javaCompiler)
        {
            javaCompiler = p_javaCompiler;
            return this;
        }
        public String getJavaCompiler()
        {
            return javaCompiler;
        }
        private String javaCompiler;

        public Data setClasspath(String p_classpath)
        {
            classpath = p_classpath;
            return this;
        }
        public String getClasspath()
        {
            return classpath;
        }
        private String classpath;

        public Data setClassLoader(ClassLoader p_classLoader)
        {
            classLoader = p_classLoader;
            return this;
        }
        public ClassLoader getClassLoader()
        {
            return classLoader;
        }
        private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();


        public Data setTemplateReplacer(TemplateReplacer p_templateReplacer)
        {
            templateReplacer = p_templateReplacer;
            return this;
        }
        public TemplateReplacer getTemplateReplacer()
        {
            return templateReplacer;
        }
        private TemplateReplacer templateReplacer;
    }

    public RecompilingTemplateManager()
    {
        this(new Data());
    }

    public RecompilingTemplateManager(Data p_data)
    {
        super(p_data.getTemplateReplacer() == null
            ? IdentityTemplateReplacer.INSTANCE
            : p_data.getTemplateReplacer());

        m_classLoader = p_data.classLoader == null
            ? getClass().getClassLoader()
            : p_data.classLoader;

        m_workDir = p_data.workDir == null
            ? getDefaultWorkDir()
            : p_data.workDir;
        m_javaCompiler = JavaCompilerFactory.makeCompiler(p_data, m_workDir, m_classLoader);
        if (TRACE) {
            trace("Java Compiler: " + m_javaCompiler.getClass().getSimpleName());
        }
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

        m_loader = AccessController.doPrivileged(new PrivilegedAction<WorkDirClassLoader>() {
            public WorkDirClassLoader run()
            {
                return new WorkDirClassLoader(m_classLoader, m_workDir);
            }
        });
    }

    public AbstractTemplateProxy.Intf constructImpl(
      AbstractTemplateProxy p_proxy, Object p_jamonContext)
    {
        AbstractTemplateProxy replacement =
          getTemplateReplacer().getReplacement(p_proxy, p_jamonContext);
        return replacement.constructImpl(getImplClass(replacement.getClass()));
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
            return getProxyClass(p_path)
                .getConstructor(new Class [] { TemplateManager.class })
                .newInstance(new Object [] { this });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void trace(String p_message)
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
            throw new RuntimeException
                ("Unable to create default work directory " + workDir);
        }
        workDir.deleteOnExit();
        return workDir.toString();
    }


    private Class<? extends AbstractTemplateImpl> getImplClass(
        Class<? extends AbstractTemplateProxy> p_proxyClass)
    {
        return getTemplateClass(StringUtils.classToTemplatePath(p_proxyClass),
                                p_proxyClass.getName() + "Impl")
                                    .asSubclass(AbstractTemplateImpl.class);
    }

    private Class<? extends AbstractTemplateProxy> getProxyClass(String p_path)
    {
        return getTemplateClass(
            p_path, StringUtils.templatePathToClassName(p_path))
            .asSubclass(AbstractTemplateProxy.class);
    }

    private Class<?> getTemplateClass(
        String p_path, String p_className)
    {
        try
        {
            try
            {
                ensureUpToDate(
                    p_path,
                    new TemplateDescriber(m_templateSource, m_loader));
                return m_loader.loadClass(p_className);
            }
            catch (ClassNotFoundException e)
            {
                if (! m_templateSource.available(p_path))
                {
                    throw new RuntimeException("The template at path " + p_path + " could not be found");
                }
                else
                {
                    throw new RuntimeException(e);
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
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

    private synchronized void ensureUpToDate(String p_path,
                                             TemplateDescriber p_describer)
        throws IOException
    {
        Collection<String> seen = new HashSet<String>();
        Collection<String> outOfDateJavaFiles = new HashSet<String>();
        List<String> workQueue = new LinkedList<String>();
        workQueue.add(p_path);

        while (!workQueue.isEmpty())
        {
            String path = workQueue.remove(0);
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

            DependencyEntry d = m_dependencyCache.get(path);
            if (d == null || d.lastUpdated() < modTime)
            {
                d = new DependencyEntry(computeDependencies(path,p_describer));
                m_dependencyCache.put(path, d);
            }
            for (String dp: d.getDependencies())
            {
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
    {
        File file = new File(p_filename);
        File parent = file.getParentFile();
        if (parent != null)
        {
            parent.mkdirs();
        }
        return file;
    }

    private void generateSource(
        String p_filename, SourceGenerator p_sourceGenerator) throws IOException
    {
        File javaFile = getWriteableFile(p_filename);
        FileOutputStream out = new FileOutputStream(javaFile);
        try
        {
            p_sourceGenerator.generateSource(out);
            out.close();
        }
        catch (IOException e)
        {
            out.close();
            javaFile.delete();
            throw e;
        }
    }

    /**
     * @return dependencies
     */
    private Collection<String> generateImpl(String p_path,
                                            TemplateDescriber p_describer)
        throws IOException
    {
        if (TRACE)
        {
            trace("generating impl for " + p_path);
        }

        TemplateUnit templateUnit = new Analyzer(p_path,p_describer).analyze();

        generateSource(
            javaImpl(p_path), new ImplGenerator(p_describer, templateUnit));
        return templateUnit.getTemplateDependencies();
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

        generateSource(javaIntf(p_path),
                       new ProxyGenerator(p_describer, p_templateUnit));
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
            return p_loader
                .loadClass(StringUtils.templatePathToClassName(p_path))
                .getAnnotation(Template.class)
                .signature();
        }
        catch (ClassNotFoundException e)
        {
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

    private String compile(Collection<String> p_sourceFiles)
    {
        if (p_sourceFiles.isEmpty())
        {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        buf.append("compiling: ");
        StringUtils.commaJoin(buf, p_sourceFiles);
        if (TRACE)
        {
            trace(buf.toString());
        }
        return m_javaCompiler.compile(p_sourceFiles.toArray(new String [p_sourceFiles.size()]));
    }

    private Collection<String> computeDependencies(
        String p_path, TemplateDescriber p_describer)
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


    private Map<String, DependencyEntry> m_dependencyCache =
        new HashMap<String, DependencyEntry>();

    private static class DependencyEntry
    {
        DependencyEntry(Collection<String> p_dependencies)
        {
            m_dependencies = p_dependencies;
            m_lastUpdated = System.currentTimeMillis();
        }

        Collection<String> m_dependencies;
        long m_lastUpdated;

        Collection<String> getDependencies()
        {
            return m_dependencies;
        }

        long lastUpdated()
        {
            return m_lastUpdated;
        }
    }

    //FIXME - use JavaLogging.
    public static final boolean TRACE =
        Boolean.valueOf(System.getProperty
                        (RecompilingTemplateManager.class.getName()
                         + ".trace" )).booleanValue();
}
