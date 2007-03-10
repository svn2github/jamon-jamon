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

import org.jamon.annotations.Argument;
import org.jamon.annotations.Template;
import org.jamon.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.Assert;

/**
 * A <code>TemplateManager</code> implementation suitable for use in
 * constructing unit tests via JUnit.
 *
 * A <code>JUnitTemplateManager<code> instance is not reusable, but
 * instead allows the "rendering" of the one template specified at
 * construction. For example, suppose the <code>/com/bar/FooTemplate</code>
 * is declared as follows:
 * <pre>
 *   &lt;%args&gt;
 *     int x;
 *     String s =&gt; "hello";
 *   &lt;/%args&gt;
 * </pre>
 *
 * To test that the method <code>showPage()</code> attempts to render
 * the <code>FooTemplate</code> with arguements <code>7</code> and
 * <code>"bye"</code>, use something like the following code:
 *
 * <pre>
 *    Map optArgs = new HashMap();
 *    optArgs.put("s", "bye");
 *    JUnitTemplateManager jtm =
 *       new JUnitTemplateManager("/com/bar/FooTemplate",
 *                                optArgs,
 *                                new Object[] { new Integer(7) });
 *
 *    TemplateManagerSource.setTemplateManager(jtm);
 *    someObj.showPage();
 *    assertTrue(jtm.getWasRendered());
 * </pre>
 */

public class JUnitTemplateManager
    implements TemplateManager,
               InvocationHandler
{
    /**
     * Construct a <code>JUnitTemplateManager</code>.
     *
     * @param p_path the template path
     * @param p_optionalArgs the expect optional arguments
     * @param p_requiredArgs the expected required argument values
     */
    public JUnitTemplateManager(String p_path,
                                Map<String, Object> p_optionalArgs,
                                Object[] p_requiredArgs)
    {
        m_path = p_path;
        m_optionalArgs = new HashMap<String, Object>(p_optionalArgs);
        m_requiredArgs = p_requiredArgs;
    }

    /**
     * Construct a <code>JUnitTemplateManager</code>.
     *
     * @param p_class the template class
     * @param p_optionalArgs the expect optional arguments
     * @param p_requiredArgs the expected required argument values
     */
    public JUnitTemplateManager(Class<? extends AbstractTemplateProxy> p_class,
                                Map<String, Object> p_optionalArgs,
                                Object[] p_requiredArgs)
    {
        this(StringUtils.classToTemplatePath(p_class),
             p_optionalArgs,
             p_requiredArgs);
    }

    /**
     * Determine if the template was successfully "rendered".
     *
     * @return whether the specified template was rendered with the
     * specified arguments
     */
    public boolean getWasRendered()
    {
        return m_rendered;
    }


    private final Map<String, Object> m_optionalArgs;
    private final Object[] m_requiredArgs;
    private final String m_path;
    private boolean m_rendered;
    private AbstractTemplateProxy.ImplData m_implData;
    private String[] m_requiredArgNames;
    private String[] m_optionalArgNames;

    public AbstractTemplateProxy.Intf constructImpl(
        AbstractTemplateProxy p_proxy)
    {
        Assert.assertTrue( m_impl == null );
        String path = StringUtils.classToTemplatePath(p_proxy.getClass());
        if (path.equals(m_path))
        {

            String className = StringUtils.templatePathToClassName(path)
                + "$Intf";
            Class<? extends AbstractTemplateProxy.Intf> intfClass;
            try
            {
                intfClass = Class.forName(className)
                    .asSubclass(AbstractTemplateProxy.Intf.class);
            }
            catch (ClassNotFoundException e)
            {
                throw new JamonRuntimeException
                    ("couldn't find class for template " + path);
            }
            catch (ClassCastException e) {
                throw new JamonRuntimeException(
                    "Impl class for template " + path
                    + " does not extend " + AbstractTemplateImpl.class.getName());
            }
            Template templateAnnotation = p_proxy.getClass().getAnnotation(Template.class);
            m_requiredArgNames = getArgNames(templateAnnotation.requiredArguments());
            m_optionalArgNames = getArgNames(templateAnnotation.optionalArguments());

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
            throw new JamonRuntimeException
                ("No template registered for " + path);
        }
    }

    public AbstractTemplateProxy constructProxy(String p_path)
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
            throw new JamonRuntimeException(e);
        }
    }

    private AbstractTemplateProxy.Intf m_impl;

    private void checkArgsLength(Method p_method,
                                 Object[] p_args,
                                 int p_expected)
    {
        Assert.assertEquals(p_method.getName() + " arg length",
                            p_expected,
                            p_args.length);
    }

    private static final Object[] EMPTY_ARGS = new Object[0];

    public Object invoke(Object p_proxy, Method p_method, Object[] p_args)
        throws Throwable
    {
        // sanity:
        Assert.assertTrue(m_impl == p_proxy);

        final Object[] args = p_args == null ? EMPTY_ARGS : p_args;

        // from the generated template Intf
        if ("render".equals(p_method.getName())
                 || "renderNoFlush".equals(p_method.getName()))
        {
            checkArgsLength(p_method, args, 1);
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

    private static String[] getArgNames(Argument[] p_arguments) {
        String[] names = new String[p_arguments.length];
        for (int i = 0; i < p_arguments.length; i++) {
            names[i] = p_arguments[i].name();
        }
        return names;
    }
}
