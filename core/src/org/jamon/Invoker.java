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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Invoker
{
    public static interface ObjectParser
    {
        Object parseObject(Class p_type, String p_value)
            throws TemplateArgumentException;
    }

    public static class InvalidTemplateException
        extends JamonException
    {
        public InvalidTemplateException(String p_templateName)
        {
            this(p_templateName, null);
        }

        public InvalidTemplateException(String p_templateName,Throwable t)
        {
            super(p_templateName
                  + " does not appear to be a valid template class",
                  t);
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

    public Invoker(TemplateManager p_manager,
                   String p_templateName)
        throws IOException
    {
        this(p_manager, p_templateName, new DefaultObjectParser());
    }

    public Invoker(TemplateManager p_manager,
                   String p_templateName,
                   ObjectParser p_objectParser)
        throws IOException
    {
        m_objectParser = p_objectParser;
        m_template = p_manager.constructProxy(p_templateName);
        m_templateClass = m_template.getClass();
        try
        {
            Constructor con = m_templateClass
                .getConstructor(new Class[] { TemplateManager.class });
            m_writeToMethod =
                m_templateClass.getMethod("writeTo",
                                          new Class [] { Writer.class });
            Method[] methods = m_templateClass.getMethods();
            Method renderMethod = null;
            for (int i = 0; i < methods.length; ++i)
            {
                if (methods[i].getName().equals("render"))
                {
                    renderMethod = methods[i];
                    break;
                }
            }
            if (renderMethod == null)
            {
                throw new InvalidTemplateException(p_templateName);
            }
            m_renderMethod = renderMethod;
        }
        catch (NoSuchMethodException e)
        {
            throw new InvalidTemplateException(p_templateName,e);
        }
    }

    public void render(Writer p_writer, Map p_argMap)
        throws InvalidTemplateException,
               IOException
    {
        try
        {
            m_writeToMethod.invoke(m_template, new Object[] { p_writer } );
            Field required = m_templateClass.getField("REQUIRED_ARG_NAMES");
            String[] requiredArgNames = (String[]) required.get(null);
            Object[] actuals = new Object[requiredArgNames.length];
            Class[] paramTypes = m_renderMethod.getParameterTypes();

            for (int i = 0; i < requiredArgNames.length; ++i)
            {
                actuals[i] =
                    parse(paramTypes[i],
                          (String) p_argMap.remove(requiredArgNames[i]));
            }
            for (Iterator i = p_argMap.keySet().iterator(); i.hasNext(); /* */)
            {
                String name = (String) i.next();
                invokeSet(name,(String) p_argMap.get(name));
            }
            m_renderMethod.invoke(m_template, actuals);
        }
        catch (NoSuchFieldException e)
        {
            throw new InvalidTemplateException(m_templateClass.getName(),e);
        }
        catch (IllegalAccessException e)
        {
            throw new InvalidTemplateException(m_templateClass.getName(),e);
        }
        catch (InvocationTargetException e)
        {
            Throwable t = e.getTargetException();
            if (t instanceof Error)
            {
                throw (Error) t;
            }
            else if (t instanceof RuntimeException)
            {
                throw (RuntimeException) t;
            }
            else
            {
                throw new InvalidTemplateException(m_templateClass.getName(),
                                                   t);
            }
        }
    }


    private final Class m_templateClass;
    private final AbstractTemplateProxy m_template;
    private final Method m_renderMethod;
    private final ObjectParser m_objectParser;
    private final Method m_writeToMethod;

    private Object parse(Class p_type, String p_value)
        throws TemplateArgumentException
    {
        return m_objectParser.parseObject(p_type, p_value);
    }

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
            throw new TemplateArgumentException("No set method for " + p_name);
        }
        Class[] paramTypes = setMethod.getParameterTypes();
        if (paramTypes.length != 1)
        {
            throw new TemplateArgumentException("Set method "
                                                + p_name
                                                + "does not take 1 arg");
        }
        try
        {
            setMethod.invoke(m_template,
                             new Object[] { parse(paramTypes[0], p_value) });
        }
        catch (IllegalAccessException e)
        {
            throw new TemplateArgumentException(e);
        }
        catch (InvocationTargetException e)
        {
            Throwable t = e.getTargetException();
            if (t instanceof Error)
            {
                throw (Error) t;
            }
            else if (t instanceof RuntimeException)
            {
                throw (RuntimeException) t;
            }
            else
            {
                throw new TemplateArgumentException(t);
            }
        }
    }

}
