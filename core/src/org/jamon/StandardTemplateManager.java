package org.modusponens.jtt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackReader;
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
import org.modusponens.jtt.node.Start;
import org.modusponens.jtt.parser.Parser;
import org.modusponens.jtt.parser.ParserException;
import org.modusponens.jtt.lexer.Lexer;
import org.modusponens.jtt.lexer.LexerException;


public class StandardTemplateManager
    implements TemplateManager
{
    public StandardTemplateManager(ClassLoader p_parentLoader)
        throws IOException
    {
        m_loader = new WorkDirLoader(p_parentLoader);
    }

    public StandardTemplateManager()
        throws IOException
    {
        this(ClassLoader.getSystemClassLoader());
    }

    public Template getInstance(String p_path, Writer p_writer)
        throws JttException
    {
        try
        {
            return (Template)
                getImplementationClass(p_path)
                    .getConstructor(new Class [] { Writer.class,
                                                   TemplateManager.class })
                    .newInstance(new Object [] { p_writer, this });
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

    public void setWorkDir(String p_workDir)
    {
        m_workDir = p_workDir;
        m_javaCompiler = null;
    }

    public void setTemplateSourceDir(String p_dir)
    {
        m_templateSourceDir = p_dir;
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
        m_javaCompiler = null;
    }

    private String getTemplateFileName(String p_path)
    {
        return m_templateSourceDir + p_path;
    }

    private String getClassName(String p_path)
    {
        return m_packagePrefix + PathUtils.pathToClassName(p_path) + "Impl";
    }

    private class WorkDirLoader
        extends ClassLoader
    {
        WorkDirLoader(ClassLoader p_parent)
        {
            super(p_parent);
        }

        private String getFileNameForClass(String p_name)
        {
            return m_workDir
                + PathUtils.classNameToPath(p_name.replace('.','/'))
                + ".class";
        }

        void invalidate()
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

            Class c = (Class) m_classMap.get(p_name);
            if (c != null)
            {
                return c;
            }
            if (m_loader == null)
            {
                try
                {
                    m_loader = new URLClassLoader
                        (new URL [] { new URL("file:" + m_workDir + "/") },
                         getParent());
                }
                catch (IOException e)
                {
                    throw new JttClassNotFoundException(e);
                }
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

    private WorkDirLoader getLoader()
    {
        return m_loader;
    }

    private Class loadAndResolveClass(String p_path)
        throws ClassNotFoundException
    {
        return getLoader().loadClass(getClassName(p_path));
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

    private String m_templateSourceDir = "testdata";
    private String m_workDir = "work";
    private String m_javac =
        System.getProperty("java.home") + FS + ".." + FS + "bin" + FS +"javac";
    private boolean m_includeRtJar = false;
    private String m_classpath = "";
    private ClassLoader m_classLoader = ClassLoader.getSystemClassLoader();
    private String m_packagePrefix = "";
    private JavaCompiler m_javaCompiler;
    private final WorkDirLoader m_loader;


    private String prefix()
    {
        if (m_packagePrefix.length() > 0
            && m_packagePrefix.charAt(m_packagePrefix.length()-1) == '.')
        {
            return m_workDir + PathUtils.classNameToPath(m_packagePrefix.substring(0,m_packagePrefix.length()-1));
        }
        else
        {
            return m_workDir + PathUtils.classNameToPath(m_packagePrefix);
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

            File tf = new File(getTemplateFileName(path));
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
            getLoader().invalidate();
        }
    }

    /**
     * @return dependencies
     */
    private Collection generateImpl(String p_path)
        throws IOException
    {
        System.err.println("generating impl for " + p_path);

        String jp = javaImpl(p_path);
        int i = jp.lastIndexOf(FS);
        if (i >= 1)
        {
            new File(jp.substring(0,i)).mkdirs();
        }

        File javaFile = new File(javaImpl(p_path));

        ImplGenerator g2 =
            new ImplGenerator(new TemplateResolver(m_packagePrefix),
                              getDescriber(),
                              p_path);

        parseTemplate(p_path).apply(g2);

        FileWriter writer = new FileWriter(javaFile);
        try
        {
            g2.generateClassSource(writer);
            writer.close();
            return g2.getCalledTemplateNames();
        }
        catch (IOException e)
        {
            writer.close();
            javaFile.delete();
            throw e;
        }
    }

    private Start parseTemplate(String p_path)
        throws IOException
    {
        try
        {
            return new Parser(new Lexer
                              (new PushbackReader
                               (new FileReader(getTemplateFileName(p_path)),
                                1024)))
                .parse();
        }
        catch (ParserException e)
        {
            throw new JttException(e);
        }
        catch (LexerException e)
        {
            throw new JttException(e);
        }
    }

    private class Describer
        implements TemplateDescriber
    {
        public List getRequiredArgNames(final String p_path)
            throws JttException
        {
            try
            {
                BaseGenerator g = new BaseGenerator();
                parseTemplate(p_path).apply(g);
                LinkedList list = new LinkedList();
                for (Iterator i = g.getRequiredArgNames(); i.hasNext(); /* */)
                {
                    list.add(i.next());
                }
                return list;
            }
            catch (IOException e)
            {
                throw new JttException(e);
            }
        }
    }

    private TemplateDescriber getDescriber()
    {
        return new Describer();
    }

    private void generateIntf(String p_path)
        throws IOException
    {
        System.err.println("generating intf for " + p_path);

        String jp = javaIntf(p_path);
        int i = jp.lastIndexOf(FS);
        if (i >= 1)
        {
            new File(jp.substring(0,i)).mkdirs();
        }

        File javaFile = new File(javaIntf(p_path));
        InterfaceGenerator g1 =
            new InterfaceGenerator(new TemplateResolver(m_packagePrefix),
                                   p_path);

        parseTemplate(p_path).apply(g1);

        FileWriter writer = new FileWriter(javaFile);

        try
        {
            g1.generateClassSource(writer);
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

        ImplGenerator g2 =
            new ImplGenerator(new TemplateResolver(m_packagePrefix),
                              getDescriber(),
                              p_path);

        parseTemplate(p_path).apply(g2);

        return g2.getCalledTemplateNames();
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
