package org.modusponens.jtt;

import java.io.File;
import java.io.Writer;
import java.io.PushbackReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.modusponens.jtt.parser.Parser;
import org.modusponens.jtt.parser.ParserException;
import org.modusponens.jtt.lexer.Lexer;
import org.modusponens.jtt.lexer.LexerException;

public class StandardTemplateManager
    implements TemplateManager
{

    private String m_templateSourceDir;
    private String m_workDir;

    public StandardTemplateManager()
    {
    }

    public void setWorkDir(String p_workDir)
    {
        m_workDir = p_workDir;
    }

    public void setTemplateSourceDir(String p_dir)
    {
        m_templateSourceDir = p_dir;
    }


    /*
      1) look for .java in workDir
      2) if not found
           look for template in srcDir
           if not found, exception
           else process template in .java file
      3) look for .class file in workDir
      4) if not found or older than .java, compile .java
      5) load class
    */

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
        if (i >= 0)
        {
            dir = p_path.substring(0,i-1);
            name = p_path.substring(i);
        }
        else
        {
            dir = "";
            name = p_path;
        }
        Phase2Generator g2 =
            new Phase2Generator(new FileWriter(getJavaFileName(p_path)),
                                dir,
                                name);
        parser.parse().apply(g2);
        g2.generateClassSource();
        return System.currentTimeMillis();
    }

    private String getJavaFileName(String p_path)
    {
        return m_workDir + p_path + "Impl.java";
    }

    private String getClassFileName(String p_path)
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
            return f.lastModified();
        }
    }

    private Class loadAndResolveClass(String p_path)
    {
        // FIXME
        return null;
    }

    private void compile(String p_path)
        throws IOException
    {
        // FIXME
    }

    private Class getClass(String p_path)
        throws IOException,
               ParserException,
               LexerException
    {
        File cf = new File(getClassFileName(p_path));
        if (cf.lastModified() < getLastModifiedJava(p_path))
        {
            compile(p_path);
        }
        return loadAndResolveClass(p_path);
    }


    public Template getInstance(String p_path, Writer p_writer)
    {
        // this method SHOULD:
        //   use its own class loader (!)
        //   check that the class exists. if it doesn't,
        //     to generate it.
        //   verify that the class is up to date. if it isn't
        //     generate it.
        try
        {
            Class c = Class.forName(p_path + "Impl");
            Constructor con =
                c.getConstructor(new Class [] { Writer.class,
                                                TemplateManager.class });
            return (Template) con.newInstance(new Object [] { p_writer, this });
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
