package org.jamon.testutils;

import org.jamon.AbstractTemplateProxy;
import org.jamon.JamonException;
import org.jamon.TemplateManager;
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

    private final Map m_optionalArgs;
    private final Object[] m_requiredArgs;
    private final String m_path;

    public AbstractTemplateProxy.Intf acquireInstance(String p_path)
        throws IOException
    {
        if (p_path.equals(m_path))
        {

            String className =
                StringUtils.templatePathToClassName(p_path) + "$Intf";
            Class intfClass;
            try
            {
                intfClass = Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
                throw new JamonException("couldn't find class for template "
                                         + p_path);
            }

            return (AbstractTemplateProxy.Intf)
                Proxy.newProxyInstance
                (getClass().getClassLoader(),
                 new Class[] { intfClass, AbstractTemplateProxy.Intf.class },
                 this);
        }
        else
        {
            throw new JamonException("No template registered for " + p_path);
        }
    }

    public void releaseInstance(AbstractTemplateProxy.Intf p_impl)
        throws IOException
    {
        // if we got here, we should be ok

        // we could assert that what we got here is what we gave out above
    }

    private Writer m_writer;

    private void checkArgsLength(Method p_method,
                                 Object[] p_args,
                                 int p_expected)
    {
        Assert.assertEquals(p_method.getName() + " arg length",
                            p_expected,
                            p_args.length);
    }

    private void checkArgument(Method p_method,
                               int p_position,
                               Object p_expected,
                               Object p_value)
    {
        Assert.assertTrue(p_method.getName() + " argument[" + p_position + "]"
                          + " expected " + p_expected + ", got " + p_value,
                          equals(p_expected, p_value));
    }

    public Object invoke(Object p_proxy, Method p_method, Object[] p_args)
        throws Throwable
    {

        // from AbstractTemplateProxy.Intf:
        if ("writeTo".equals(p_method.getName()))
        {
            checkArgsLength(p_method,p_args,1);
            m_writer = (Writer) p_args[0];
            return null;
        }
        else if ("escaping".equals(p_method.getName()))
        {
            checkArgsLength(p_method,p_args,1);
            return null;
        }
        else if ("initialize".equals(p_method.getName()))
        {
            checkArgsLength(p_method,p_args,0);
            return null;
        }
        else if ("autoFlush".equals(p_method.getName()))
        {
            checkArgsLength(p_method,p_args,1);
            return null;
        }
        else if ("getPath".equals(p_method.getName()))
        {
            checkArgsLength(p_method,p_args,0);
            return m_path;
        }

        // from the generated template Intf
        else if ("render".equals(p_method.getName()))
        {
            Assert.assertTrue("all optional arguments not set before render",
                              0 == m_optionalArgs.size());
            checkArgsLength(p_method,
                            p_args,
                            m_requiredArgs.length);
            if (m_requiredArgs.length != p_args.length)
            {
                throw new IllegalStateException("shouldn't get here");
            }
            for (int i = 0; i < m_requiredArgs.length; ++i)
            {
                checkArgument(p_method, i, m_requiredArgs[i], p_args[i]);
            }
            return null;
        }
        else if (p_method.getName().startsWith("set"))
        {
            checkArgsLength(p_method,p_args,1);
            String argname = p_method.getName().substring(3);
            argname = Character.toLowerCase(argname.charAt(0))
                + argname.substring(1);
            Assert.assertTrue("unexpected optional argument " + argname,
                              m_optionalArgs.containsKey(argname));
            checkArgument(p_method, 0, m_optionalArgs.remove(argname), p_args[0]);
            return null;
        }
        else
        {
            // ?
            throw new IllegalArgumentException("Unexpected method "
                                               + p_method);
        }
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
