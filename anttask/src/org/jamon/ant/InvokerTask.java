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

package org.jamon.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment;

import org.jamon.Invoker;
import org.jamon.TemplateManager;
import org.jamon.BasicTemplateManager;
import org.jamon.RecompilingTemplateManager;
import org.jamon.JamonTemplateException;

/**
 * Ant task to reflectively invoke templates.
 **/

public class InvokerTask
    extends Task
{
    public InvokerTask()
    {
        m_recompilingManagerData = new RecompilingTemplateManager.Data();
    }

    public void execute()
        throws BuildException
    {
        Properties sysprops = (Properties) System.getProperties().clone();
        try
        {
            for (Iterator p = m_sysprops.iterator(); p.hasNext(); )
            {
                Environment.Variable var = (Environment.Variable) p.next();
                System.setProperty(var.getKey(), var.getValue());
            }
            Writer writer;
            if (m_output == null)
            {
                writer = new OutputStreamWriter(System.out);
            }
            else
            {
                File parent = m_output.getAbsoluteFile().getParentFile();
                if (parent != null)
                {
                    // FIXME: should we check for failure here
                    //   or let it fall through?
                    parent.mkdirs();
                }
                writer = new FileWriter(m_output);
            }
            TemplateManager manager =
                m_dynamicRecompilation
                ? new RecompilingTemplateManager(m_recompilingManagerData)
                : (TemplateManager) new BasicTemplateManager();
            new Invoker(manager, m_path).render(writer, m_args);
        }
        catch (Invoker.InvalidTemplateException e)
        {
            throw new BuildException(e);
        }
        catch (JamonTemplateException e)
        {
            throw new BuildException(e.getMessage(),
                                     new JamonLocation(e.getFileName(),
                                                       e.getLine(),
                                                       e.getColumn()));
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        finally
        {
            System.setProperties(sysprops);
        }
    }

    public void setCompiler(String p_javac)
    {
        m_recompilingManagerData.setJavaCompiler(p_javac);
        // slight hack but convenient
        if (p_javac.toLowerCase().endsWith("jikes"))
        {
            m_recompilingManagerData.setJavaCompilerNeedsRtJar(true);
        }
    }

    public void setClasspath(Path p_classpath)
        throws IOException
    {
        String[] paths = p_classpath.list();
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < urls.length; ++i)
        {
            urls[i] = new URL(
                "file",
                null,
                paths[i] + (new File(paths[i]).isDirectory() ? "/" : ""));
        }
        m_recompilingManagerData
            .setClassLoader(new URLClassLoader(urls,
                                               getClass().getClassLoader()));
        m_recompilingManagerData.setClasspath(p_classpath.toString());
    }

    public void setWorkDir(File p_workDir)
    {
        m_recompilingManagerData.setWorkDir(p_workDir.getAbsolutePath());
    }

    public void setSourceDir(File p_sourceDir)
    {
        m_recompilingManagerData.setSourceDir(p_sourceDir.getAbsolutePath());
    }

    public void setOutput(File p_output)
        throws IOException
    {
        m_output = p_output;
    }

    public void setTemplate(String p_path)
    {
        m_path = p_path;
    }

    public void setDynamicRecompilation(boolean p_dynamicRecompilation)
    {
        m_dynamicRecompilation = p_dynamicRecompilation;
    }

    private final RecompilingTemplateManager.Data m_recompilingManagerData;
    private boolean m_dynamicRecompilation = true;
    private String m_path;
    private HashMap m_args = new HashMap();
    private Collection m_sysprops = new HashSet();
    private File m_output;

    public void addSysproperty(Environment.Variable p_property)
    {
        m_sysprops.add(p_property);
    }

    public void addConfiguredArg(Arg p_arg)
    {
        m_args.put(p_arg.m_name, p_arg.m_value);
    }

    public static class Arg
    {
        public void setName(String p_name)
        {
            m_name = p_name;
        }
        public void setValue(String p_value)
        {
            m_value = p_value;
        }
        private String m_name;
        private String m_value;
    }
}
