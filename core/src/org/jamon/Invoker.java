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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


/**
 * An <code>Invoker</code> manages the reflective rendering of a
 * template, given a template path and a <code>Map</code> of argument
 * values as Strings.
 *
 * This class could certainly use some refactoring, and in fact, the
 * public contract ought to allow reuse for multiple templates.
 */

public class Invoker
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

    /**
     * Construct an <code>Invoker</code> with a template manager and
     * template path, using a {@link DefaultObjectParser}.
     *
     * @param p_manager the <code>TemplateManager</code> to use
     * @param p_templateName the path of the template to be rendered
     */
    public Invoker(TemplateManager p_manager,
                   String p_templateName)
        throws IOException
    {
        this(p_manager, p_templateName, new DefaultObjectParser());
    }

    /**
     * Construct an <code>Invoker</code> for a template path, using a
     * {@link DefaultObjectParser} and the default {@link
     * TemplateManager} as determined via the {@link
     * TemplateManagerSource}.
     *
     * @param p_templateName the path of the template to be rendered
     */
    public Invoker(String p_templateName)
        throws IOException
    {
        this(p_templateName,
             new DefaultObjectParser());
    }

    /**
     * Construct an <code>Invoker</code> for a template path using the
     * specified {@ObjectParser}, and the default {@link
     * TemplateManager} as determined via the {@link
     * TemplateManagerSource}.
     *
     * @param p_templateName the path of the template to be rendered
     * @param p_objectParser the object with which to translate
     */
    public Invoker(String p_templateName, ObjectParser p_objectParser)
        throws IOException
    {
        this(TemplateManagerSource.getTemplateManagerFor(p_templateName),
             p_templateName,
             p_objectParser);
    }


    /**
     * Construct an <code>Invoker</code> with a template manager,
     * template path and a specific <code>ObjectParser</code>.
     *
     * @param p_manager the <code>TemplateManager</code> to use
     * @param p_templateName the path of the template to be rendered
     * @param p_objectParser the object with which to translate
     * strings to values.
     */
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
            m_requiredArgNames = Arrays.asList
                ((String[])
                 m_templateClass.getField("REQUIRED_ARG_NAMES").get(null));
            m_optionalArgNames = Arrays.asList
                ((String[])
                 m_templateClass.getField("OPTIONAL_ARG_NAMES").get(null));
        }
        catch (NoSuchMethodException e)
        {
            throw new InvalidTemplateException(p_templateName, e);
        }
        catch (NoSuchFieldException e)
        {
            throw new InvalidTemplateException(p_templateName, e);
        }
        catch (IllegalAccessException e)
        {
            throw new InvalidTemplateException(p_templateName, e);
        }
    }

    /**
     * Render the template.
     *
     * @param p_writer the Writer to render to
     * @param p_argMap a Map&lt;String,String&gt; of arguments
     */
    public void render(Writer p_writer, Map p_argMap)
        throws InvalidTemplateException,
               IOException
    {
        render(p_writer, p_argMap, false);
    }

    /**
     * Render the template.
     *
     * @param p_writer the Writer to render to
     * @param p_argMap a Map&lt;String,String&gt; of arguments
     * @param p_ignoreUnusedParams whether to throw an exception if
     * "extra" arguments are supplied
     */
    public void render(Writer p_writer,
                       Map p_argMap,
                       boolean p_ignoreUnusedParams)
        throws InvalidTemplateException,
               IOException
    {
        try
        {
            if (! p_ignoreUnusedParams)
            {
                validateArguments(p_argMap);
            }

            invokeOptionalArguments(p_argMap);
            m_renderMethod.invoke(m_template,
                                  computeRenderArguments(p_argMap, p_writer));
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

    public List getRequiredArgumentNames()
    {
        return m_requiredArgNames;
    }

    public List getOptionalArgumentNames()
    {
        return m_optionalArgNames;
    }

    public Class getArgumentType(String p_argName)
    {
        if (m_optionalArgNames.contains(p_argName))
        {
            return findSetMethod(p_argName).getParameterTypes()[0];
        }
        else
        {
            int i = m_requiredArgNames.indexOf(p_argName);
            if (i < 0)
            {
                return null;
            }
            else
            {
                return m_renderMethod.getParameterTypes()[i+1];
            }
        }
    }

    private Object[] computeRenderArguments(Map p_argMap, Writer p_writer)
        throws TemplateArgumentException
    {
        Object[] actuals = new Object[1 + m_requiredArgNames.size()];
        actuals[0] = p_writer;

        Class[] paramTypes = m_renderMethod.getParameterTypes();

        for (int i = 0; i < m_requiredArgNames.size(); ++i)
        {
            actuals[i + 1] =
                parse(paramTypes[i + 1],
                      (String) p_argMap.get(m_requiredArgNames.get(i)));
        }
        return actuals;
    }

    private void invokeOptionalArguments(Map p_argMap)
        throws TemplateArgumentException
    {
        for (int i = 0; i < m_optionalArgNames.size(); ++i)
        {
            String name = (String) m_optionalArgNames.get(i);
            if (p_argMap.containsKey(name))
            {
                invokeSet(name, (String) p_argMap.get(name));
            }
        }
    }

    private void validateArguments(Map p_argMap)
        throws JamonException
    {
        Set argNames = new HashSet();
        argNames.addAll(p_argMap.keySet());
        argNames.removeAll(m_requiredArgNames);
        argNames.removeAll(m_optionalArgNames);
        if (! argNames.isEmpty())
        {
            StringBuffer msg =
                new StringBuffer("Unknown arguments supplied: ");
            for (Iterator i = argNames.iterator(); i.hasNext(); )
            {
                msg.append(i.next());
                if (i.hasNext())
                {
                    msg.append(",");
                }
            }
            throw new JamonException(msg.toString());
        }
    }

    private Object parse(Class p_type, String p_value)
        throws TemplateArgumentException
    {
        return m_objectParser.parseObject(p_type, p_value);
    }

    private Method findSetMethod(String p_name)
    {
        Method[] methods = m_templateClass.getMethods();
        String name = "set"
            + Character.toUpperCase(p_name.charAt(0))
            + p_name.substring(1);
        for (int i = 0; i < methods.length; ++i)
        {
            if (methods[i].getName().equals(name))
            {
                Class[] paramTypes = methods[i].getParameterTypes();
                if (paramTypes.length == 1)
                {
                    return methods[i];
                }
            }
        }
        return null;
    }

    private void invokeSet(String p_name, String p_value)
        throws TemplateArgumentException
    {
        Method setMethod = findSetMethod(p_name);
        if (setMethod == null)
        {
            throw new TemplateArgumentException("No set method for " + p_name);
        }
        try
        {
            setMethod.invoke(m_template,
                             new Object[] {
                                 parse(setMethod.getParameterTypes()[0],
                                       p_value)
                             });
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


    private final Class m_templateClass;
    private final AbstractTemplateProxy m_template;
    private final Method m_renderMethod;
    private final ObjectParser m_objectParser;
    private final List m_requiredArgNames;
    private final List m_optionalArgNames;
}
