package org.modusponens.jtt;

import java.io.IOException;
import java.io.InputStream;

public class JavaCompiler
{
    private final String m_javac;
    private final String m_classPath;

    public JavaCompiler(String p_javac, String p_classPath)
    {
        m_javac = p_javac;
        m_classPath = p_classPath;
    }

    public JavaCompiler(String p_classPath)
    {
        this(System.getProperty("java.home") + "/../bin/javac", p_classPath);
    }

    public JavaCompiler()
    {
        this(System.getProperty("java.class.path"));
    }

    public void compile(String p_javaFile)
        throws IOException
    {
        String [] cmdline = new String [] { m_javac,
                                            "-classpath",
                                            m_classPath,
                                            p_javaFile };

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
            throw new IOException("Compilation failed code="
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

}
