package org.modusponens.jtt;

import java.io.File;
import java.io.Writer;
import java.io.PushbackReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    public StandardTemplateManager(ClassLoader p_parentLoader)
    {
        m_loader = new Loader(p_parentLoader);
    }

    public StandardTemplateManager()
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
            dir = p_path.substring(1,i).replace('/','.');
            new File(m_workDir + p_path).mkdirs();
            name = p_path.substring(i+1);
        }
        else
        {
            dir = "";
            name = p_path;
        }
        FileWriter w = new FileWriter(getJavaFileName(p_path));
        ImplGenerator g2 = new ImplGenerator(w,dir,name);
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

    private class Loader
        extends ClassLoader
    {
        Loader(ClassLoader p_parent)
        {
            super(p_parent);
        }

        private byte [] readBytesForClass(String p_name)
            throws IOException
        {
            FileInputStream s =
                new FileInputStream(getClassFileNameForClass(p_name));
            final byte [] buf = new byte[1024];
            byte [] bytes = new byte[0];
            while (true)
            {
                int i = s.read(buf);
                if (i <= 0)
                {
                    break;
                }
                byte [] newbytes = new byte[bytes.length + i];
                System.arraycopy(bytes,0,newbytes,0,bytes.length);
                System.arraycopy(buf,0,newbytes,bytes.length,i);
                bytes = newbytes;
            }
            return bytes;
        }

        private String getClassFileNameForClass(String p_name)
        {
            return m_workDir + "/" + p_name.replace('.','/') + ".class";
        }

        protected Class findClass(String p_name)
            throws ClassNotFoundException
        {
            try
            {
                byte [] bytecode = readBytesForClass(p_name);
                return defineClass(p_name,bytecode,0,bytecode.length);
            }
            catch (IOException e)
            {
                throw new JttClassNotFoundException(e);
            }
        }
    }

    private final Loader m_loader;

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

    private void compile(String p_path)
        throws IOException
    {
        String [] cmdline = new String [] { m_javac,
                                            "-classpath",
                                            getClassPath(),
                                            getJavaFileName(p_path) };

        Process p = Runtime.getRuntime().exec(cmdline);
        StreamConsumer stderr = new StreamConsumer(p.getErrorStream());
        Thread errThread = new Thread(stderr);
        errThread.start();
        int code = -1;
        try
        {
            code = p.waitFor();
        }
        catch (InterruptedException e)
        {
            errThread.interrupt();
        }

        try
        {
            errThread.join();
        }
        catch (InterruptedException e)
        {
            // just ignore it
        }
        if (code != 0)
        {
            throw new JttException("Compilation failed code="
                                   + code
                                   + "\n"
                                   + stderr.getContents());
        }
    }

    private static class StreamConsumer
        implements Runnable
    {
        StreamConsumer(InputStream p_stream)
        {
            m_stream = p_stream;
        }
        private final InputStream m_stream;
        private final StringBuffer m_buffer = new StringBuffer();

        synchronized String getContents()
        {
            return m_buffer.toString();
        }

        public void run()
        {
            final byte [] buf = new byte[1024];
            boolean eof = false;
            while (! eof)
            {
                try
                {
                    int read = m_stream.read(buf);
                    if (read == -1)
                    {
                        eof = true;
                    }
                    else if (read == 0)
                    {
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e)
                        {
                            // FIXME: really?
                            eof = true;
                        }
                    }
                    else
                    {
                        synchronized (m_buffer)
                        {
                            m_buffer.append(new String(buf,0,read));
                        }
                    }
                }
                catch (IOException e)
                {
                    // FIXME: what here?
                    eof = true;
                }
            }
        }
    }

    private Class getClass(String p_path)
        throws IOException,
               ParserException,
               LexerException,
               ClassNotFoundException
    {
        File cf = new File(getClassFileName(p_path));
        if (cf.lastModified() < getLastModifiedJava(p_path))
        {
            compile(p_path);
        }
        return loadAndResolveClass(p_path);
    }


    public Template getInstance(String p_path, Writer p_writer)
        throws JttException
    {
        try
        {
            Class c = getClass(p_path);
            Constructor con =
                c.getConstructor(new Class [] { Writer.class,
                                                TemplateManager.class });
            return (Template) con.newInstance(new Object [] { p_writer, this });
        }
        catch (Exception e)
        {
            throw new JttException(e);
        }
    }
}
