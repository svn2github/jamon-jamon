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

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Invoker
{
    Invoker(String[] args)
        throws UsageException,
               TemplateArgumentException,
               IOException,
               InvalidTemplateException
    {
        int a = 0;
        try
        {
            StandardTemplateManager manager = new StandardTemplateManager();
            String outFile = null;
            while (a < args.length && args[a].startsWith("-"))
            {
                if (args[a].startsWith("--workdir="))
                {
                    manager.setWorkDir(args[a].substring(10));
                }
                else if (args[a].equals("-w"))
                {
                    a++;
                    if (a < args.length)
                    {
                        manager.setWorkDir(args[a]);
                    }
                    else
                    {
                        throw new UsageException();
                    }
                }
                else if (args[a].startsWith("--srcdir="))
                {
                    manager.setSourceDir(args[a].substring(9));
                }
                else if (args[a].equals("-s"))
                {
                    a++;
                    if (a < args.length)
                    {
                        manager.setSourceDir(args[a]);
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
            AbstractTemplateImpl impl = manager.getInstance(args[a]);
            manager.releaseInstance(impl);
            String className = impl.getClass().getName();
            className = className.substring(0,className.length()-4);
            m_templateClass =
                manager.getWorkClassLoader().loadClass(className);
            Constructor con = m_templateClass
                .getConstructor(new Class[] { TemplateManager.class });
            Method writeToMethod =
                m_templateClass.getMethod("writeTo",
                                          new Class [] { Writer.class });
            Method[] methods = m_templateClass.getMethods();
            Method render = null;
            for (int i = 0; i < methods.length; ++i)
            {
                if (methods[i].getName().equals("render"))
                {
                    render = methods[i];
                    break;
                }
            }
            if (render == null)
            {
                throw new InvalidTemplateException(args[a]);
            }
            m_renderMethod = render;
            m_argMap = new HashMap();
            for (int i = a+1; i < args.length; ++i)
            {
                parseArgString(args[i]);
            }

            m_template = (AbstractTemplateProxy)
                con.newInstance(new Object[]{ manager });
            Writer writer = outFile == null
                ? new OutputStreamWriter(System.out)
                : new FileWriter(outFile);
            writeToMethod.invoke(m_template, new Object[] { writer } );

        }
        catch (ClassNotFoundException e)
        {
            throw new InvalidTemplateException(args[a]);
        }
        catch (IllegalAccessException e)
        {
            throw new InvalidTemplateException(args[a]);
        }
        catch (InvocationTargetException e)
        {
            throw new InvalidTemplateException(args[a]);
        }
        catch (NoSuchMethodException e)
        {
            throw new InvalidTemplateException(args[a]);
        }
        catch (InstantiationException e)
        {
            throw new InvalidTemplateException(args[a]);
        }
    }

    private void parseArgString(String p_arg)
        throws UsageException
    {
        int i = p_arg.indexOf("=");
        if (i <= 0)
        {
            throw new UsageException();
        }
        m_argMap.put(p_arg.substring(0,i),p_arg.substring(i+1));
    }

    private Object parse(Class p_type, String p_string)
        throws TemplateArgumentException
    {
        try
        {
            if (p_string == null)
            {
                if (p_type.isPrimitive())
                {
                    throw new TemplateArgumentException();
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
        catch (NumberFormatException e)
        {
            throw new TemplateArgumentException();
        }
        catch (NoSuchMethodException e)
        {
            throw new TemplateArgumentException();
        }
        catch (InvocationTargetException e)
        {
            throw new TemplateArgumentException();
        }
        catch (InstantiationException e)
        {
            throw new TemplateArgumentException();
        }
        catch (IllegalAccessException e)
        {
            throw new TemplateArgumentException();
        }
    }

    private final Class m_templateClass;
    private final AbstractTemplateProxy m_template;
    private final Method m_renderMethod;
    private final Map m_argMap;


    private void invokeSet(String p_name, String p_value)
        throws TemplateArgumentException
    {
        Method[] methods = m_templateClass.getMethods();
        String name = "set"
            + Character.toUpperCase(p_name.charAt(0))
            + p_name.substring(1);
        Method setMethod = null;
        for (int i = 0; i < methods.length; ++i)
        {
            if (methods[i].getName().equals(name))
            {
                setMethod = methods[i];
            }
        }
        if (setMethod == null)
        {
            throw new TemplateArgumentException();
        }
        Class[] paramTypes = setMethod.getParameterTypes();
        if (paramTypes.length != 1)
        {
            throw new TemplateArgumentException();
        }
        try
        {
            setMethod.invoke(m_template,
                             new Object[] { parse(paramTypes[0], p_value) });
        }
        catch (IllegalAccessException e)
        {
            throw new TemplateArgumentException();
        }
        catch (InvocationTargetException e)
        {
            throw new TemplateArgumentException();
        }
    }

    void renderTemplate()
        throws InvalidTemplateException
    {
        try
        {
            Field required = m_templateClass.getField("REQUIRED_ARGS");
            String[] requiredArgNames = (String[]) required.get(null);
            Object[] actuals = new Object[requiredArgNames.length];
            Class[] paramTypes = m_renderMethod.getParameterTypes();

            for (int i = 0; i < requiredArgNames.length; ++i)
            {
                actuals[i] =
                    parse(paramTypes[i],
                          (String) m_argMap.remove(requiredArgNames[i]));
            }
            for (Iterator i = m_argMap.keySet().iterator(); i.hasNext(); /* */)
            {
                String name = (String) i.next();
                invokeSet(name,(String) m_argMap.get(name));
            }
            m_renderMethod.invoke(m_template, actuals);
        }
        catch (TemplateArgumentException e)
        {
            displayError("supplied arguments are not valid");
        }
        catch (NoSuchFieldException e)
        {
            throw new InvalidTemplateException(m_templateClass.getName());
        }
        catch (IllegalAccessException e)
        {
            displayError("Unable to render template");
        }
        catch (InvocationTargetException e)
        {
            displayError("Unable to render template");
        }
    }

    private static void displayError(String p_message)
    {
        System.err.println(p_message);
    }

    public static void main(String[] args)
    {
        try
        {
            new Invoker(args).renderTemplate();
        }
        catch (UsageException e)
        {
            displayError("Usage: " + e.usage());
        }
        catch (InvalidTemplateException e)
        {
            e.printStackTrace();
            displayError(e.getMessage());
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }

    private static class InvalidTemplateException
        extends Exception
    {
        InvalidTemplateException(String p_templateName)
        {
            super(p_templateName
                  + " does not appear to be a valid template class");
        }
    }

    private static class TemplateArgumentException
        extends Exception
    {
    }


    private static class UsageException
        extends Exception
    {
        String usage()
        {
            return "java "
                + Invoker.class.getName()
                + " [-o outputfile] "
                + " [-s templatesourcedir]"
                + " [-w workdir]"
                + " template-path [[arg1=val1] ...]";
        }
    }

}
