package org.modusponens.jtt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.modusponens.jtt.node.Start;


public class StandardTemplateManager
    implements TemplateManager
{
    public StandardTemplateManager(ClassLoader p_parentLoader,
                                   String p_templateSourceDir,
                                   String p_workDir)
        throws IOException
    {
        m_workDir = p_workDir;
        m_loader = new WorkDirClassLoader(p_parentLoader, m_workDir);
        m_describer = new TemplateDescriber(p_templateSourceDir);
    }

    public StandardTemplateManager(String p_templateSourceDir,
                                   String p_workDir)
        throws IOException
    {
        this(ClassLoader.getSystemClassLoader(),
             p_templateSourceDir,
             p_workDir);
    }

    public Template getInstance(String p_path, Writer p_writer)
        throws JttException
    {
        return getInstance(p_path, p_writer, this);
    }

    public Template getInstance(String p_path,
                                Writer p_writer,
                                TemplateManager p_manager)
        throws JttException
    {
        try
        {
            return (Template)
                getImplementationClass(p_path)
                    .getConstructor(new Class [] { Writer.class,
                                                   TemplateManager.class })
                    .newInstance(new Object [] { p_writer, p_manager });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JttException(e);
        }
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

    public void setPackagePrefix(String p_packagePrefix)
    {
        m_packagePrefix = p_packagePrefix == null ? "" : p_packagePrefix;
    }

    private String getClassName(String p_path)
    {
        return m_packagePrefix + StringUtils.pathToClassName(p_path) + "Impl";
    }

    private Class loadAndResolveClass(String p_path)
        throws ClassNotFoundException
    {
        return m_loader.loadClass(getClassName(p_path));
    }

    private String getClassPath()
    {
        String cp = m_classpath != null ? (m_classpath + PS) : "";
        cp = m_workDir + PS + cp + System.getProperty("java.class.path");
        if (m_includeRtJar)
        {
            cp += PS + System.getProperty("java.home") + FS + "lib" + FS +"rt.jar";
        }
        return cp;
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

        ensureUpToDate(p_path);
        return loadAndResolveClass(p_path);
    }

    private static final String PS = System.getProperty("path.separator");
    private static final String FS = System.getProperty("file.separator");

    private final TemplateDescriber m_describer;
    private final String m_workDir;
    private String m_javac =
        System.getProperty("java.home") + FS + ".." + FS + "bin" + FS +"javac";
    private boolean m_includeRtJar = false;
    private String m_classpath = "";
    private ClassLoader m_classLoader = ClassLoader.getSystemClassLoader();
    private String m_packagePrefix = "";
    private JavaCompiler m_javaCompiler;
    private final WorkDirClassLoader m_loader;


    private String prefix()
    {
        if (m_packagePrefix.length() > 0
            && m_packagePrefix.charAt(m_packagePrefix.length()-1) == '.')
        {
            return m_workDir + StringUtils.classNameToPath(m_packagePrefix.substring(0,m_packagePrefix.length()-1));
        }
        else
        {
            return m_workDir + StringUtils.classNameToPath(m_packagePrefix);
        }
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
            System.err.println("processing " + path);
            seen.add(path);

            File tf = m_describer.getTemplateFile(path);
            if (!tf.exists())
            {
                throw new JttException("Missing template " + path);
            }

            File jm = new File(javaImpl(path));
            File ji = new File(javaIntf(path));
            long ts = Math.min(jm.lastModified(),ji.lastModified());
            if (jm.lastModified() < tf.lastModified()
                || ji.lastModified() < tf.lastModified())
            {
                generateIntf(path);
                m_dependencyCache.put(path,
                                      new DependencyEntry(generateImpl(path)));
                ts = System.currentTimeMillis();

            }
            File cm = new File(classImpl(path));
            File ci = new File(classIntf(path));
            if (cm.lastModified() < ts || ci.lastModified() < ts)
            {
                outOfDateJavaFiles.add(javaImpl(path));
                outOfDateJavaFiles.add(javaIntf(path));
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
    private Collection generateImpl(String p_path)
        throws IOException
    {
        System.err.println("generating impl for " + p_path);

        ImplAnalyzer ia =
            new ImplAnalyzer(p_path,
                             m_describer.parseTemplate(p_path));

        File javaFile = getWriteableFile(javaImpl(p_path));
        FileWriter writer = new FileWriter(javaFile);
        try
        {
            new ImplGenerator(writer,
                              new TemplateResolver(m_packagePrefix),
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

    private void generateIntf(String p_path)
        throws IOException
    {
        System.err.println("generating intf for " + p_path);

        BaseAnalyzer bg =
            new BaseAnalyzer(m_describer.parseTemplate(p_path));

        File javaFile = getWriteableFile(javaIntf(p_path));
        FileWriter writer = new FileWriter(javaFile);
        try
        {
            new IntfGenerator(new TemplateResolver(m_packagePrefix),
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
    }

    private void compile(Collection p_sourceFiles)
        throws IOException
    {
        if (p_sourceFiles.isEmpty())
        {
            return;
        }

        System.err.print("compiling: ");
        for (Iterator i = p_sourceFiles.iterator(); i.hasNext(); /* */)
        {
            System.err.print(i.next());
            if (i.hasNext())
            {
                System.err.print(", ");
            }
        }
        System.err.println();
        getJavaCompiler()
            .compile((String []) p_sourceFiles.toArray(new String [0]));
    }

    private Collection computeDependencies(String p_path)
        throws IOException
    {
        System.err.println("computing dependencies for " + p_path);

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
