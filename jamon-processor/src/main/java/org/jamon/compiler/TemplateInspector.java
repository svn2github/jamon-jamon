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

package org.jamon.compiler;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jamon.AbstractTemplateProxy;
import org.jamon.TemplateManager;
import org.jamon.TemplateManagerSource;
import org.jamon.annotations.Argument;
import org.jamon.annotations.Template;
import org.jamon.util.StringUtils;

/**
 * An <code>TemplateInspector</code> manages the reflective rendering of a
 * template, given a template path and a <code>Map</code> of argument
 * values as Strings.
 *
 * This class could certainly use some refactoring, and in fact, the
 * public contract ought to allow reuse for multiple templates.
 */

public class TemplateInspector
{
    public static class InvalidTemplateException
        extends Exception
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

        private static final long serialVersionUID = 2006091701L;
    }

    /**
     * Construct an <code>TemplateInspector</code> for a template path
     * using the default {@link TemplateManager} as determined via the
     * {@link TemplateManagerSource}.
     *
     * @param p_templateName the path of the template to be rendered
     */
    public TemplateInspector(String p_templateName)
        throws InvalidTemplateException
    {
        this(TemplateManagerSource.getTemplateManagerFor(p_templateName),
             p_templateName);
    }


    /**
     * Construct an <code>TemplateInspector</code> with a template
     * manager, template path.
     *
     * @param p_manager the <code>TemplateManager</code> to use
     * @param p_templateName the path of the template to be rendered
     */
    public TemplateInspector(TemplateManager p_manager, String p_templateName)
        throws InvalidTemplateException
    {
        m_template = p_manager.constructProxy(p_templateName);
        m_templateClass = m_template.getClass();
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
        Template templateAnnotation = m_templateClass.getAnnotation(Template.class);
        m_requiredArgNames = getArgNames(templateAnnotation.requiredArguments());
        m_optionalArgNames = getArgNames(templateAnnotation.optionalArguments());
    }

    private List<String> getArgNames(Argument[] p_arguments)
    {
        List<String> argumentNames = new ArrayList<String>(p_arguments.length);
        for (Argument argument : p_arguments)
        {
            argumentNames.add(argument.name());
        }
        return argumentNames;
    }

    /**
     * Render the template.
     *
     * @param p_writer the Writer to render to
     * @param p_argMap a Map&lt;String,String&gt; of arguments
     */
    public void render(Writer p_writer, Map<String, Object> p_argMap)
        throws InvalidTemplateException,
               UnknownArgumentsException
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
                       Map<String, Object> p_argMap,
                       boolean p_ignoreUnusedParams)
        throws InvalidTemplateException,
               UnknownArgumentsException
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

    public List<String> getRequiredArgumentNames()
    {
        return m_requiredArgNames;
    }

    public List<String> getOptionalArgumentNames()
    {
        return m_optionalArgNames;
    }

    public Class<?> getArgumentType(String p_argName)
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

    private Object[] computeRenderArguments(Map<String, Object> p_argMap,
                                            Writer p_writer)
    {
        Object[] actuals = new Object[1 + m_requiredArgNames.size()];
        actuals[0] = p_writer;

        for (int i = 0; i < m_requiredArgNames.size(); ++i)
        {
            actuals[i + 1] = p_argMap.get(m_requiredArgNames.get(i));
        }
        return actuals;
    }

    private void invokeOptionalArguments(Map<String, Object> p_argMap)
        throws InvalidTemplateException
    {
        for (int i = 0; i < m_optionalArgNames.size(); ++i)
        {
            String name = m_optionalArgNames.get(i);
            if (p_argMap.containsKey(name))
            {
                invokeSet(name, p_argMap.get(name));
            }
        }
    }

    public static class UnknownArgumentsException
        extends JamonException
    {
        private static final long serialVersionUID = 2006091701L;

        UnknownArgumentsException(String p_msg)
        {
            super(p_msg);
        }
    }

    private void validateArguments(Map<String, Object> p_argMap)
        throws UnknownArgumentsException
    {
        Set<String> argNames = new HashSet<String>();
        argNames.addAll(p_argMap.keySet());
        argNames.removeAll(m_requiredArgNames);
        argNames.removeAll(m_optionalArgNames);
        if (! argNames.isEmpty())
        {
            StringBuilder msg =
                new StringBuilder("Unknown arguments supplied: ");
            StringUtils.commaJoin(msg, argNames);
            throw new UnknownArgumentsException(msg.toString());
        }
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
                if (methods[i].getParameterTypes().length == 1)
                {
                    return methods[i];
                }
            }
        }
        return null;
    }

    private void invokeSet(String p_name, Object p_value)
        throws InvalidTemplateException
    {
        Method setMethod = findSetMethod(p_name);
        if (setMethod == null)
        {
            throw new InvalidTemplateException(m_templateClass.getName() +
                                               " has no set method for "
                                               + p_name);
        }
        try
        {
            setMethod.invoke(m_template, new Object[] { p_value });
        }
        catch (IllegalAccessException e)
        {
            throw new InvalidTemplateException(m_templateClass.getName(), e);
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


    private final Class<?> m_templateClass;
    private final AbstractTemplateProxy m_template;
    private final Method m_renderMethod;
    private final List<String> m_requiredArgNames;
    private final List<String> m_optionalArgNames;
}
