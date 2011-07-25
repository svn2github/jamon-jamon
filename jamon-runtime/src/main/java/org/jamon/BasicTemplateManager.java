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

package org.jamon;

import org.jamon.AbstractTemplateProxy.Intf;

/**
 * A standard implementation of the {@link TemplateManager} interface. The
 * <code>BasicTemplateManager</code> is geared towards production deployment; it is designed for
 * performance. It will <b>NOT</b> dynamically examine or recompile template sources.
 * <code>BasicTemplateManager</code> instances are thread-safe. In your applications, you generally
 * want exactly one instance of a BasicTemplateManager (i.e. a singleton), so consider using
 * {@link TemplateManagerSource}
 **/

public class BasicTemplateManager extends AbstractTemplateManager {

  private final ClassLoader classLoader;

  /**
   * Creates a new <code>BasicTemplateManager</code> using a default <code>ClassLoader</code>.
   **/
  public BasicTemplateManager() {
    this(null);
  }

  /**
   * Creates a new <code>BasicTemplateManager</code> from a specified <code>ClassLoader</code>.
   *
   * @param classLoader the <code>ClassLoader</code> to use to load templates.
   **/
  public BasicTemplateManager(ClassLoader classLoader) {
    this(classLoader, IdentityTemplateReplacer.INSTANCE);
  }

  /**
   * Creates a new <code>BasicTemplateManager</code> from a specified <code>ClassLoader</code>.
   *
   * @param classLoader the <code>ClassLoader</code> to use to load templates.
   * @param templateReplacer the {@code TemplateReplacer} to use for replacing templates.
   **/
  public BasicTemplateManager(ClassLoader classLoader, TemplateReplacer templateReplacer) {
    super(templateReplacer);
    this.classLoader = classLoader == null
        ? getClass().getClassLoader()
        : classLoader;
  }

  @Override
  protected Intf constructImplFromReplacedProxy(AbstractTemplateProxy replacedProxy) {
    return replacedProxy.constructImpl();
  }

  /**
   * Given a template path, return a proxy for that template.
   *
   * @param path the path to the template
   * @return a <code>Template</code> proxy instance
   **/
  @Override
  public AbstractTemplateProxy constructProxy(String path) {
    try {
      return getProxyClass(path).getConstructor(new Class[] { TemplateManager.class })
          .newInstance(new Object[] { this });
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("The template at path " + path + " could not be found");
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Class<? extends AbstractTemplateProxy> getProxyClass(String path) throws ClassNotFoundException {
    String strippedPath = stripLeadingSlashes(path);
    return classLoader.loadClass(strippedPath.replace('/', '.')).asSubclass(
      AbstractTemplateProxy.class);
  }

  private static String stripLeadingSlashes(String path) {
    int firstNonSlash = 0;
    while (path.indexOf('/', firstNonSlash) == firstNonSlash) {
      firstNonSlash++;
    }
    return path.substring(firstNonSlash);
  }
}
