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
 * Contributor(s):
 */

package org.jamon;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.HashMap;

public class InvokerTool
{

    /**
     * An <code>ObjectParser</code> describes how to convert string
     * values to objects.
     */
    public static interface ObjectParser
    {
        Object parseObject(Class p_type, String p_value)
            throws TemplateArgumentException;
    }

    public static class DefaultObjectParser
        implements ObjectParser
    {
        public Object parseObject(Class p_type, String p_string)
            throws TemplateArgumentException
        {
            try
            {
                if (p_string == null)
                {
                    if (p_type.isPrimitive())
                    {
                        throw new TemplateArgumentException
                            ("primitive types cannot be null");
                    }
                    else
                    {
                        return null;
                    }
                }
                else if (p_type == String.class)
                {
                    return p_string;
                }
                else if (p_type == Boolean.class || p_type == Boolean.TYPE )
                {
                    return Boolean.valueOf(p_string);
                }
                else if (p_type == Integer.class || p_type == Integer.TYPE)
                {
                    return Integer.valueOf(p_string);
                }
                else if (p_type == Float.class || p_type == Float.TYPE)
                {
                    return Float.valueOf(p_string);
                }
                else if (p_type == Double.class || p_type == Double.TYPE)
                {
                    return Double.valueOf(p_string);
                }
                else if (p_type == Short.class || p_type == Short.TYPE)
                {
                    return Short.valueOf(p_string);
                }
                else if (p_type == Byte.class || p_type == Byte.TYPE)
                {
                    return Byte.valueOf(p_string);
                }
                else
                {
                    return p_type
                        .getConstructor(new Class[] { String.class })
                        .newInstance(new Object[] { p_string });
                }
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new TemplateArgumentException(e);
            }
        }
    }

    public static class TemplateArgumentException
        extends JamonException
    {
        public TemplateArgumentException(Throwable t)
        {
            super(t);
        }

        public TemplateArgumentException(String p_msg)
        {
            super(p_msg);
        }
    }

    public class UsageException
        extends Exception
    {
        public String toString()
        {
            return "java "
                + InvokerTool.this.getClass().getName()
                + " [-o outputfile] "
                + " [-s templatesourcedir]"
                + " [-w workdir]"
                + " template-path [[arg1=val1] ...]";
        }
    }

    public InvokerTool(ObjectParser p_objectParser)
    {
        m_objectParser = p_objectParser;
    }

    private final ObjectParser m_objectParser;

    public InvokerTool()
    {
        this(new DefaultObjectParser());
    }

    private void parseArgString(TemplateInspector p_inspector,
                                Map p_argMap,
                                String p_arg)
        throws UsageException, TemplateArgumentException
    {
        int i = p_arg.indexOf("=");
        if (i <= 0)
        {
            throw new UsageException();
        }
        String name = p_arg.substring(0,i);
        p_argMap.put(name,
                     m_objectParser.parseObject
                     (p_inspector.getArgumentType(name),
                      p_arg.substring(i+1)));
    }

    protected void invoke(String[] args)
        throws UsageException,
               IOException,
               TemplateArgumentException,
               TemplateInspector.UnknownArgumentsException,
               TemplateInspector.InvalidTemplateException
    {
        int a = 0;
        RecompilingTemplateManager.Data data =
            new RecompilingTemplateManager.Data();
        String outFile = null;
        while (a < args.length && args[a].startsWith("-"))
        {
            if (args[a].startsWith("--workdir="))
            {
                data.setWorkDir(args[a].substring(10));
            }
            else if (args[a].equals("-w"))
            {
                a++;
                if (a < args.length)
                {
                    data.setWorkDir(args[a]);
                }
                else
                {
                    throw new UsageException();
                }
            }
            else if (args[a].startsWith("--srcdir="))
            {
                data.setSourceDir(args[a].substring(9));
            }
            else if (args[a].equals("-s"))
            {
                a++;
                if (a < args.length)
                {
                    data.setSourceDir(args[a]);
                }
                else
                {
                    throw new UsageException();
                }
            }
            else if (args[a].startsWith("--output="))
            {
                outFile = args[a].substring(9);
            }
            else if (args[a].equals("-o"))
            {
                a++;
                if (a < args.length)
                {
                    outFile = args[a];
                }
                else
                {
                    throw new UsageException();
                }
            }
            else
            {
                throw new UsageException();
            }
            a++;
        }
        if (a >= args.length)
        {
            throw new UsageException();
        }

        String templateName = args[a++];

        TemplateInspector inspector =
            new TemplateInspector(new RecompilingTemplateManager(data),
                                  templateName);

        HashMap argMap = new HashMap();
        while (a < args.length)
        {
            parseArgString(inspector, argMap, args[a++]);
        }

        Writer writer = outFile == null
            ? new OutputStreamWriter(System.out)
            : new FileWriter(outFile);

        inspector.render(writer, argMap);
    }

    public static void main(String[] args)
        throws Exception
    {
        try
        {
            new InvokerTool().invoke(args);
        }
        catch (UsageException e)
        {
            System.err.println("Usage: " + e);
        }
    }
}
