package org.modusponens.jtt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.net.URLClassLoader;
import java.net.URL;
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
        m_loader = new URLClassLoader(new URL []
                                      {
                                          new URL("file:" + m_workDir + FS)
                                      },
                                      p_parentLoader);
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
        m_packagePrefix = p_packagePrefix;
        m_javaCompiler = null;
    }

    private String getJavaFileName(String p_path)
    {
        return m_workDir + p_path + "Impl.java";
    }

    private String getJavaClassFileName(String p_path)
    {
        return m_workDir + p_path + "Impl.class";
    }

    private String getTemplateFileName(String p_path)
    {
        return m_templateSourceDir + p_path;
    }

    private String getClassName(String p_path)
    {
        return PathUtils.pathToClassName(p_path) + "Impl";
    }

    private Class loadAndResolveClass(String p_path)
        throws ClassNotFoundException
    {
        // FIXME: need to check that it still implements the interface
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

    private void createJavaFile(String p_path)
        throws IOException,
               ParserException,
               LexerException
    {
        Parser parser =
            new Parser(new Lexer(new PushbackReader
                                 (new FileReader(getTemplateFileName(p_path)),
                                  1024)));
        int i = p_path.lastIndexOf(FS);
        String dir, name;
        if (i >= 1)
        {
            new File(m_workDir + p_path.substring(0,i)).mkdirs();
            dir = PathUtils.pathToClassName(p_path.substring(1,i));
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
    }

    private boolean isJavaFileUpToDate(String p_path)
        throws IOException
    {
        File jf = new File(getJavaFileName(p_path));
        if (jf.exists())
        {
            File tf = new File(getTemplateFileName(p_path));
            if (! tf.exists() )
            {
                throw new JttException("Template file "
                                       + p_path
                                       + " missing?!");
            }
            return tf.lastModified() < jf.lastModified();
        }
        else
        {
            return false;
        }
    }

    private boolean isClassFileUpToDate(String p_path)
        throws IOException
    {
        File jf = new File(getJavaFileName(p_path));
        if (!jf.exists())
        {
            throw new IOException("java file disappeared!?");
        }
        File cf = new File(getJavaClassFileName(p_path));
        return jf.lastModified() < cf.lastModified();
    }

    private Class getImplementationClass(String p_path)
        throws IOException,
               ParserException,
               LexerException,
               ClassNotFoundException
    {
        if (! isJavaFileUpToDate(p_path))
        {
            createJavaFile(p_path);
        }

        if (! isClassFileUpToDate(p_path) )
        {
            getJavaCompiler().compile(getJavaFileName(p_path));
        }

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
    private final ClassLoader m_loader;
}
