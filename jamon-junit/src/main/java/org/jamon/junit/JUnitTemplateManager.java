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

package org.jamon.junit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jamon.AbstractTemplateImpl;
import org.jamon.AbstractTemplateManager;
import org.jamon.AbstractTemplateProxy;
import org.jamon.TemplateManager;
import org.jamon.AbstractTemplateProxy.Intf;
import org.jamon.annotations.Argument;
import org.jamon.annotations.Template;

/**
 * A <code>TemplateManager</code> implementation suitable for use in constructing unit tests via
 * JUnit. A <code>JUnitTemplateManager<code> instance is not reusable, but
 * instead allows the "rendering" of the one template specified at
 * construction. For example, suppose the <code>/com/bar/FooTemplate</code> is declared as follows:
 *
 * <pre>
 *   &lt;%args&gt;
 *     int x;
 *     String s =&gt; "hello";
 *   &lt;/%args&gt;
 * </pre>
 *
 * To test that the method <code>showPage()</code> attempts to render the <code>FooTemplate</code>
 * with arguements <code>7</code> and <code>"bye"</code>, use something like the following code:
 *
 * <pre>
 * Map optArgs = new HashMap();
 * optArgs.put(&quot;s&quot;, &quot;bye&quot;);
 * JUnitTemplateManager jtm = new JUnitTemplateManager(&quot;/com/bar/FooTemplate&quot;, optArgs,
 *     new Object[] { new Integer(7) });
 *
 * TemplateManagerSource.setTemplateManager(jtm);
 * someObj.showPage();
 * assertTrue(jtm.getWasRendered());
 * </pre>
 */

public class JUnitTemplateManager extends AbstractTemplateManager implements InvocationHandler {
  /**
   * Construct a <code>JUnitTemplateManager</code>.
   *
   * @param path the template path
   * @param optionalArgs the expect optional arguments
   * @param requiredArgs the expected required argument values
   */
  public JUnitTemplateManager(
    String path, Map<String, Object> optionalArgs, Object[] requiredArgs) {
    this.path = path;
    this.optionalArgs = new HashMap<String, Object>(optionalArgs);
    this.requiredArgs = requiredArgs == null
        ? null
        : requiredArgs.clone();
  }

  /**
   * Construct a <code>JUnitTemplateManager</code>.
   *
   * @param clazz the template class
   * @param optionalArgs the expect optional arguments
   * @param requiredArgs the expected required argument values
   */
  public JUnitTemplateManager(
    Class<? extends AbstractTemplateProxy> clazz,
    Map<String, Object> optionalArgs,
    Object[] requiredArgs) {
    this(classToTemplatePath(clazz), optionalArgs, requiredArgs);
  }

  /**
   * Determine if the template was successfully "rendered".
   *
   * @return whether the specified template was rendered with the specified arguments
   */
  public boolean getWasRendered() {
    return rendered;
  }

  private final Map<String, Object> optionalArgs;
  private final Object[] requiredArgs;
  private final String path;
  private boolean rendered;
  private AbstractTemplateProxy.ImplData implData;
  private String[] requiredArgNames;
  private String[] optionalArgNames;

  @Override
  public AbstractTemplateProxy.Intf constructImpl(
    AbstractTemplateProxy proxy, Object jamonContext) {
    Assert.assertTrue(m_impl == null);
    String tempatePath = classToTemplatePath(proxy.getClass());
    if (tempatePath.equals(path)) {

      String className = templatePathToClassName(tempatePath) + "$Intf";
      Class<? extends AbstractTemplateProxy.Intf> intfClass;
      try {
        intfClass = Class.forName(className).asSubclass(AbstractTemplateProxy.Intf.class);
      }
      catch (ClassNotFoundException e) {
        throw new RuntimeException("couldn't find class for template " + tempatePath);
      }
      catch (ClassCastException e) {
        throw new RuntimeException(
          "Impl class for template " + tempatePath + " does not extend "
          + AbstractTemplateImpl.class.getName());
      }
      Template templateAnnotation = proxy.getClass().getAnnotation(Template.class);
      requiredArgNames = getArgNames(templateAnnotation.requiredArguments());
      optionalArgNames = getArgNames(templateAnnotation.optionalArguments());

      implData = proxy.getImplData();
      m_impl = (AbstractTemplateProxy.Intf) Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[] { intfClass, AbstractTemplateProxy.Intf.class },
        this);
      return m_impl;
    }
    else {
      throw new RuntimeException("No template registered for " + tempatePath);
    }
  }

  @Override
  protected Intf constructImplFromReplacedProxy(AbstractTemplateProxy replacedProxy) {
    // for now, let's not worry about template replacement.
    throw new IllegalStateException();
  }

  @Override
  public AbstractTemplateProxy constructProxy(String path) {
    try {
      return (AbstractTemplateProxy) Class.forName(templatePathToClassName(path))
        .getConstructor(new Class[] { TemplateManager.class })
        .newInstance(new Object[] { this });
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private AbstractTemplateProxy.Intf m_impl;

  private void checkArgsLength(Method method, Object[] args, int expected) {
    Assert.assertEquals(method.getName() + " arg length", expected, args.length);
  }

  private static final Object[] EMPTY_ARGS = new Object[0];

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // sanity:
    Assert.assertTrue(m_impl == proxy);

    final Object[] nonNullArgs = args == null
        ? EMPTY_ARGS
        : args;

    // from the generated template Intf
    if ("render".equals(method.getName()) || "renderNoFlush".equals(method.getName())) {
      checkArgsLength(method, nonNullArgs, 1);
      checkArgValues();
      rendered = true;
      return null;
    }
    else {
      // ?
      throw new IllegalArgumentException("Unexpected method " + method);
    }
  }

  private void checkArgValues() throws Exception {
    Assert.assertEquals("required arg length mismatch", requiredArgNames.length,
      requiredArgs.length);
    for (int i = 0; i < requiredArgNames.length; i++) {
      Assert.assertEquals("required argument " + requiredArgNames[i], requiredArgs[i],
        getArgValue(requiredArgNames[i]));
    }
    for (int i = 0; i < optionalArgNames.length; i++) {
      checkOptionalArgument(optionalArgNames[i], optionalArgs
          .containsKey(optionalArgNames[i]));
    }
  }

  private void checkOptionalArgument(String name, boolean defaultNotExpected) throws Exception {
    Assert.assertTrue("optional argument " + name + (defaultNotExpected
        ? " not"
        : "") + " set", Boolean.valueOf(defaultNotExpected).equals(getIsNotDefault(name)));
    if (defaultNotExpected) {
      Assert.assertEquals("optional argument " + name, optionalArgs.get(name),
        getArgValue(name));
    }
  }

  private Object getIsNotDefault(String name) throws Exception {
    return implData.getClass().getMethod("get" + capitalize(name) + "__IsNotDefault", new Class[0])
      .invoke(implData, new Object[0]);
  }

  private Object getArgValue(String name) throws Exception {
    return implData.getClass().getMethod("get" + capitalize(name), new Class[0])
      .invoke(implData, new Object[0]);
  }

  private static String[] getArgNames(Argument[] arguments) {
    String[] names = new String[arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      names[i] = arguments[i].name();
    }
    return names;
  }

  private static String capitalize(String string) {
    if (string == null) {
      return null;
    }
    else {
      char[] chars = string.toCharArray();
      if (chars.length == 0) {
        return string;
      }
      else {
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
      }
    }
  }

  private static String templatePathToClassName(String string) {
    int i = 0;
    while (i < string.length() && string.charAt(i) == '/') {
      i++;
    }
    return string.substring(i).replace('/', '.');
  }

  private static String classToTemplatePath(Class<?> clazz) {
    return clazz.getName().replace('.', '/');
  }
}
