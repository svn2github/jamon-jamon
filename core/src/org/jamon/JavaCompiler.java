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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon;

import java.io.File;
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
        throws IOException
    {
        // FIXME: does this work on windows? mac?
        this(new File(new File(System.getProperty("java.home")).getParent(),
                      "bin" + File.separator +"javac")
                 .getCanonicalPath(),
             p_classPath);
    }

    public JavaCompiler()
        throws IOException
    {
        this(System.getProperty("java.class.path"));
    }

    public void compile(String p_javaFile)
        throws IOException
    {
        compile(new String [] { p_javaFile });
    }

    public void compile(String [] p_javaFiles)
        throws IOException
    {
        String [] cmdline = new String[p_javaFiles.length + 3];
        System.arraycopy(p_javaFiles,0,cmdline,3,p_javaFiles.length);
        cmdline[0] = m_javac;
        cmdline[1] = "-classpath";
        cmdline[2] = m_classPath;

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
