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

import org.jamon.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.Assert;

public class JUnitTemplateManager
    implements TemplateManager,
               InvocationHandler
{
    public JUnitTemplateManager(String p_path,
                                Map p_optionalArgs,
                                Object[] p_requiredArgs)
    {
        m_path = p_path;
        m_optionalArgs = new HashMap(p_optionalArgs);
        m_requiredArgs = p_requiredArgs;
    }

    public JUnitTemplateManager(Class p_class,
                                Map p_optionalArgs,
                                Object[] p_requiredArgs)
    {
        this(StringUtils.classToTemplatePath(p_class),
             p_optionalArgs,
             p_requiredArgs);
    }

    private final Map m_optionalArgs;
    private final Object[] m_requiredArgs;
    private final String m_path;
    private boolean m_rendered;
    private AbstractTemplateProxy.ImplData m_implData;
    private String[] m_requiredArgNames;
    private String[] m_optionalArgNames;

    public AbstractTemplateProxy.Intf constructImpl(
        AbstractTemplateProxy p_proxy)
        throws IOException
    {
        Assert.assertTrue( m_impl == null );
        String path = StringUtils.classToTemplatePath(p_proxy.getClass());
        if (path.equals(m_path))
        {

            String className = StringUtils.templatePathToClassName(path)
                + "$Intf";
            Class intfClass;
            try
            {
                intfClass = Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
                throw new JamonException("couldn't find class for template "
                                         + path);
            }

            try
            {
                m_requiredArgNames = (String[]) p_proxy
                    .getClass().getField("REQUIRED_ARG_NAMES").get(null);
                m_optionalArgNames = (String[]) p_proxy
                    .getClass().getField("OPTIONAL_ARG_NAMES").get(null);
            }
            catch (Exception e)
            {
                throw new JamonException(e);
            }

            m_implData = p_proxy.getImplData();
            m_impl = (AbstractTemplateProxy.Intf)
                Proxy.newProxyInstance
                (getClass().getClassLoader(),
                 new Class[] { intfClass, AbstractTemplateProxy.Intf.class },
                 this);
            return m_impl;
        }
        else
        {
            throw new JamonException("No template registered for " + path);
        }
    }

    public AbstractTemplateProxy constructProxy(String p_path)
        throws IOException
    {
        try
        {
            return (AbstractTemplateProxy)
                Class.forName(StringUtils.templatePathToClassName(p_path))
                .getConstructor(new Class[] { TemplateManager.class })
                .newInstance(new Object[] { this });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JamonException(e);
        }
    }

    private AbstractTemplateProxy.Intf m_impl;
    private Writer m_writer;

    private void checkArgsLength(Method p_method,
                                 Object[] p_args,
                                 int p_expected)
    {
        Assert.assertEquals(p_method.getName() + " arg length",
                            p_expected,
                            p_args.length);
    }

    public Object invoke(Object p_proxy, Method p_method, Object[] p_args)
        throws Throwable
    {
        // sanity:
        Assert.assertTrue( m_impl == p_proxy );

        if (p_args == null)
        {
            p_args = new Object[0];
        }

        // from AbstractTemplateProxy.Intf:
        if ("escapeWith".equals(p_method.getName()))
        {
            checkArgsLength(p_method,p_args,1);
            return null;
        }

        // from the generated template Intf
        else if ("render".equals(p_method.getName()))
        {
            checkArgsLength(p_method, p_args, 0);
            checkArgValues();
            m_rendered = true;
            return null;
        }
        else
        {
            // ?
            throw new IllegalArgumentException("Unexpected method "
                                               + p_method);
        }
    }

    private void checkArgValues()
        throws Exception
    {
        Assert.assertEquals("required arg length mismatch",
                            m_requiredArgNames.length, m_requiredArgs.length);
        for (int i = 0; i < m_requiredArgNames.length; i++)
        {
             Assert.assertEquals("required argument " + m_requiredArgNames[i],
                                 m_requiredArgs[i],
                                 getArgValue(m_requiredArgNames[i]));
        }
        for (int i = 0; i < m_optionalArgNames.length; i++)
        {
            checkOptionalArgument(m_optionalArgNames[i],
                                  m_optionalArgs.containsKey(
                                      m_optionalArgNames[i]));
        }
    }

    private void checkOptionalArgument(String p_name,
                                       boolean p_defaultNotExpected)
        throws Exception
    {
        Assert.assertTrue("optional argument " + p_name
                          + (p_defaultNotExpected ? " not" : "")
                          + " set",
                          new Boolean(p_defaultNotExpected)
                              .equals(getIsNotDefault(p_name)));
        if(p_defaultNotExpected)
        {
            Assert.assertEquals("optional argument " + p_name,
                                m_optionalArgs.get(p_name),
                                getArgValue(p_name));
        }
    }

    private Object getIsNotDefault(String p_name)
        throws Exception
    {
        return m_implData.getClass()
            .getMethod("get"
                       + StringUtils.capitalize(p_name)
                       + "__IsNotDefault",new Class[0])
            .invoke(m_implData,new Object[0]);
    }

    private Object getArgValue(String p_name)
        throws Exception
    {
        return m_implData.getClass()
            .getMethod("get"
                       + StringUtils.capitalize(p_name),
                       new Class[0])
            .invoke(m_implData, new Object[0]);
    }

    public boolean getWasRendered()
    {
        return m_rendered;
    }

    private boolean equals(Object p_obj1, Object p_obj2)
    {
        if (p_obj1 == null)
        {
            return p_obj2 == null;
        }
        else if (p_obj2 == null)
        {
            return false;
        }
        else if (p_obj1 instanceof Object[])
        {
            if (p_obj2 instanceof Object[])
            {
                Object[] a1 = (Object[]) p_obj1;
                Object[] a2 = (Object[]) p_obj2;
                if (a1.length == a2.length)
                {
                    for (int i = 0; i < a1.length; ++i)
                    {
                        if (!equals(a1[i],a2[i]))
                        {
                            return false;
                        }
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return p_obj1.equals(p_obj2);
        }
    }
}
