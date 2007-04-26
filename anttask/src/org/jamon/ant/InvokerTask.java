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
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.jamon.InvokerTool;
import org.jamon.JamonException;
import org.jamon.ParserErrorsImpl;
import org.jamon.RecompilingTemplateManager;
import org.jamon.TemplateInspector;
import org.jamon.TemplateInspector.InvalidTemplateException;
import org.jamon.api.ParserError;
import org.jamon.runtime.BasicTemplateManager;
import org.jamon.runtime.TemplateManager;

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

    private Writer computeWriter()
        throws IOException
    {
        if (m_outputPropertyName != null)
        {
            return new StringWriter();
        }
        else if (m_output != null)
        {
            File parent = m_output.getAbsoluteFile().getParentFile();
            if (parent != null)
            {
                // FIXME: should we check for failure here
                //   or let it fall through?
                parent.mkdirs();
            }
            return new FileWriter(m_output);
        }
        else
        {
            return new OutputStreamWriter(System.out);
        }
    }

    @Override
    public void execute()
        throws BuildException
    {
        Properties sysprops = (Properties) System.getProperties().clone();
        try
        {
            for (Environment.Variable var : m_sysprops)
            {
                System.setProperty(var.getKey(), var.getValue());
            }
            Writer writer = computeWriter();
            TemplateManager manager =
                m_dynamicRecompilation
                ? new RecompilingTemplateManager(m_recompilingManagerData)
                : (TemplateManager) new BasicTemplateManager(m_classLoader);

            TemplateInspector inspector =
                new TemplateInspector(manager, m_path);

            convertArguments(inspector);

            inspector.render(writer, m_args);
            if (m_outputPropertyName != null)
            {
                getProject().setProperty(m_outputPropertyName,
                                         writer.toString());
            }
        }
        catch (InvalidTemplateException e) {
            throw new BuildException(e);
        }
        catch (JamonException e)
        {
            throw new BuildException(e);
        }
        catch (ParserErrorsImpl e)
        {
            e.printErrors(System.err); //FIXME - is this the right thing to do?
            if (! e.getErrors().isEmpty())
            {
                ParserError error = e.getErrors().get(0);
                throw new BuildException(
                    error.getMessage(), new JamonLocation(error.getLocation()));
            }
            else
            {
                throw new BuildException("Jamon translation failed");
            }
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


    private void convertArguments(TemplateInspector p_inspector)
        throws InvokerTool.TemplateArgumentException
    {
        InvokerTool.ObjectParser parser =
            new InvokerTool.DefaultObjectParser();
        for (Map.Entry<String, Object> entry : m_args.entrySet())
        {
            entry.setValue
                (parser.parseObject
                 (p_inspector.getArgumentType(entry.getKey()),
                  (String)entry.getValue()));
        }
    }

    public void setProperty(String p_outputPropertyName)
    {
        if (m_output != null)
        {
            throw new BuildException("Can't specify both output file and output property name");
        }
        m_outputPropertyName = p_outputPropertyName;
    }

    public void setCompiler(String p_javac)
    {
        if (p_javac != null && p_javac.length() > 0)
        {
            m_recompilingManagerData.setJavaCompiler(p_javac);
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
        m_classLoader = new URLClassLoader(urls,
                                           getClass().getClassLoader());
        m_recompilingManagerData.setClassLoader(m_classLoader);
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
    {
        if (m_outputPropertyName != null)
        {
            throw new BuildException("Can't specify both output file and output property name");
        }
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
    private HashMap<String, Object> m_args = new HashMap<String, Object>();
    private Collection<Environment.Variable> m_sysprops =
        new HashSet<Environment.Variable>();
    private File m_output;
    private String m_outputPropertyName;
    private ClassLoader m_classLoader;

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
