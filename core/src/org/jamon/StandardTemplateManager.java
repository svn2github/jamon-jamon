package org.modusponens.jtt;

import java.io.File;
import java.io.Writer;
import java.io.PushbackReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.net.URL;
import org.modusponens.jtt.parser.Parser;
import org.modusponens.jtt.parser.ParserException;
import org.modusponens.jtt.lexer.Lexer;
import org.modusponens.jtt.lexer.LexerException;

public class StandardTemplateManager
    implements TemplateManager
{

    private String m_templateSourceDir = "testdata";
    private String m_workDir = "work";
    private String m_javac =
        System.getProperty("java.home") + "/../bin/javac";
    private boolean m_includeRtJar = false;
    private String m_classpath = "";
    private ClassLoader m_classLoader = ClassLoader.getSystemClassLoader();
    private String m_packagePrefix = "";

    public StandardTemplateManager(ClassLoader p_parentLoader)
        throws IOException
    {
        m_loader = new URLClassLoader(new URL []
                                      {
                                          new URL("file:" + m_workDir + "/")
                                      },
                                      p_parentLoader);
    }

    public StandardTemplateManager()
        throws IOException
    {
        this(ClassLoader.getSystemClassLoader());
    }

    public void setWorkDir(String p_workDir)
    {
        m_workDir = p_workDir;
    }

    public void setTemplateSourceDir(String p_dir)
    {
        m_templateSourceDir = p_dir;
    }

    public void setJavaCompiler(String p_javac)
    {
        m_javac = p_javac;
    }

    public void setJavaCompilerNeedsRtJar(boolean p_includeRtJar)
    {
        m_includeRtJar = p_includeRtJar;
    }

    public void setClasspath(String p_classpath)
    {
        m_classpath = p_classpath;
    }

    public void setPackagePrefix(String p_packagePrefix)
    {
        m_packagePrefix = p_packagePrefix;
    }

    private long createJavaFile(String p_path)
        throws IOException,
               ParserException,
               LexerException
    {
        Parser parser =
            new Parser(new Lexer(new PushbackReader
                                 (new FileReader(getTemplateFileName(p_path)),
                                  1024)));
        int i = p_path.lastIndexOf('/');
        String dir, name;
        if (i >= 1)
        {
            new File(m_workDir + p_path.substring(0,i)).mkdirs();
            dir = p_path.substring(1,i).replace('/','.');
            name = p_path.substring(i+1);
        }
        else if (i == 0)
        {
            dir = "";
            name = p_path.substring(1);
        }
        else
        {
            dir = "";
            name = p_path;
        }
        FileWriter w = new FileWriter(getJavaFileName(p_path));
        ImplGenerator g2 = new ImplGenerator(w,m_packagePrefix,dir,name);
        parser.parse().apply(g2);
        try
        {
            g2.generateClassSource();
            w.close();
        }
        catch (IOException e)
        {
            w.close();
            new File(getJavaFileName(p_path)).delete();
            throw e;
        }
        return System.currentTimeMillis();
    }

    private String getJavaFileName(String p_path)
    {
        return m_workDir + p_path + "Impl.java";
    }

    private String getImplementationClassFileName(String p_path)
    {
        return m_workDir + p_path + "Impl.class";
    }

    private String getTemplateFileName(String p_path)
    {
        return m_templateSourceDir + p_path;
    }

    private long getLastModifiedJava(String p_path)
        throws IOException,
               ParserException,
               LexerException
    {
        File f = new File(getJavaFileName(p_path));
        if (! f.exists())
        {
            return createJavaFile(p_path);
        }
        else
        {
            File tf = new File(getTemplateFileName(p_path));
            if (! tf.exists() )
            {
                throw new JttException("Template file "
                                       + p_path
                                       + " missing!");
            }
            if ( tf.lastModified() > f.lastModified() )
            {
                return createJavaFile(p_path);
            }
            else
            {
                return f.lastModified();
            }
        }
    }

    private final ClassLoader m_loader;

    private String getClassName(String p_path)
    {
        // FIXME
        return p_path.replace('/','.').substring(1);
    }

    private Class loadAndResolveClass(String p_path)
        throws ClassNotFoundException
    {
        // FIXME: need to check that it still implements the interface
        return m_loader.loadClass(getClassName(p_path)+"Impl");
    }

    private String getClassPath()
    {
        String cp = m_classpath != null ? (m_classpath + ':') : "";
        cp = m_workDir + ":" + cp + System.getProperty("java.class.path");
        if (m_includeRtJar)
        {
            cp += ':' + System.getProperty("java.home") + "/lib/rt.jar";
        }
        return cp;
    }

    private Class getImplementationClass(String p_path)
        throws IOException,
               ParserException,
               LexerException,
               ClassNotFoundException
    {
        File cf = new File(getImplementationClassFileName(p_path));
        if (cf.lastModified() < getLastModifiedJava(p_path))
        {
            new JavaCompiler(m_javac, getClassPath())
                .compile(getJavaFileName(p_path));
        }
        return loadAndResolveClass(p_path);
    }


    public Template getInstance(String p_path, Writer p_writer)
        throws JttException
    {
        try
        {
            Class c = getImplementationClass(p_path);
            Constructor con =
                c.getConstructor(new Class [] { Writer.class,
                                                TemplateManager.class });
            return (Template) con.newInstance(new Object [] { p_writer, this });
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
}
