/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
 * An <code>TemplateInspector</code> manages the reflective rendering of a template, given a
 * template path and a <code>Map</code> of argument values as Strings. This class could certainly
 * use some refactoring, and in fact, the public contract ought to allow reuse for multiple
 * templates.
 */

public class TemplateInspector {
  public static class InvalidTemplateException extends Exception {
    public InvalidTemplateException(String p_templateName) {
      this(p_templateName, null);
    }

    public InvalidTemplateException(String p_templateName, Throwable t) {
      super(p_templateName + " does not appear to be a valid template class", t);
    }

    private static final long serialVersionUID = 2006091701L;
  }

  /**
   * Construct an <code>TemplateInspector</code> for a template path using the default
   * {@link TemplateManager} as determined via the {@link TemplateManagerSource}.
   *
   * @param p_templateName the path of the template to be rendered
   */
  public TemplateInspector(String p_templateName) throws InvalidTemplateException {
    this(TemplateManagerSource.getTemplateManagerFor(p_templateName), p_templateName);
  }

  /**
   * Construct an <code>TemplateInspector</code> with a template manager, template path.
   *
   * @param p_manager the <code>TemplateManager</code> to use
   * @param p_templateName the path of the template to be rendered
   */
  public TemplateInspector(TemplateManager manager, String templateName)
      throws InvalidTemplateException {
    template = manager.constructProxy(templateName);
    templateClass = template.getClass();
    Method[] methods = templateClass.getMethods();
    Method renderMethod = null;
    for (int i = 0; i < methods.length; ++i) {
      if (methods[i].getName().equals("render")) {
        renderMethod = methods[i];
        break;
      }
    }
    if (renderMethod == null) {
      throw new InvalidTemplateException(templateName);
    }
    this.renderMethod = renderMethod;
    Template templateAnnotation = templateClass.getAnnotation(Template.class);
    requiredArgNames = getArgNames(templateAnnotation.requiredArguments());
    optionalArgNames = getArgNames(templateAnnotation.optionalArguments());
  }

  private List<String> getArgNames(Argument[] arguments) {
    List<String> argumentNames = new ArrayList<String>(arguments.length);
    for (Argument argument : arguments) {
      argumentNames.add(argument.name());
    }
    return argumentNames;
  }

  /**
   * Render the template.
   *
   * @param writer the Writer to render to
   * @param argMap a Map&lt;String,String&gt; of arguments
   */
  public void render(Writer writer, Map<String, Object> argMap) throws InvalidTemplateException,
    UnknownArgumentsException {
    render(writer, argMap, false);
  }

  /**
   * Render the template.
   *
   * @param writer the Writer to render to
   * @param argMap a Map&lt;String,String&gt; of arguments
   * @param ignoreUnusedParams whether to throw an exception if "extra" arguments are supplied
   */
  public void render(Writer writer, Map<String, Object> argMap, boolean ignoreUnusedParams)
  throws InvalidTemplateException,
    UnknownArgumentsException {
    try {
      if (!ignoreUnusedParams) {
        validateArguments(argMap);
      }

      invokeOptionalArguments(argMap);
      renderMethod.invoke(template, computeRenderArguments(argMap, writer));
    }
    catch (IllegalAccessException e) {
      throw new InvalidTemplateException(templateClass.getName(), e);
    }
    catch (InvocationTargetException e) {
      Throwable t = e.getTargetException();
      if (t instanceof Error) {
        throw (Error) t;
      }
      else if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      }
      else {
        throw new InvalidTemplateException(templateClass.getName(), t);
      }
    }
  }

  public List<String> getRequiredArgumentNames() {
    return requiredArgNames;
  }

  public List<String> getOptionalArgumentNames() {
    return optionalArgNames;
  }

  public Class<?> getArgumentType(String argName) {
    if (optionalArgNames.contains(argName)) {
      return findSetMethod(argName).getParameterTypes()[0];
    }
    else {
      int i = requiredArgNames.indexOf(argName);
      if (i < 0) {
        return null;
      }
      else {
        return renderMethod.getParameterTypes()[i + 1];
      }
    }
  }

  private Object[] computeRenderArguments(Map<String, Object> argMap, Writer writer) {
    Object[] actuals = new Object[1 + requiredArgNames.size()];
    actuals[0] = writer;

    for (int i = 0; i < requiredArgNames.size(); ++i) {
      actuals[i + 1] = argMap.get(requiredArgNames.get(i));
    }
    return actuals;
  }

  private void invokeOptionalArguments(Map<String, Object> argMap)
  throws InvalidTemplateException {
    for (int i = 0; i < optionalArgNames.size(); ++i) {
      String name = optionalArgNames.get(i);
      if (argMap.containsKey(name)) {
        invokeSet(name, argMap.get(name));
      }
    }
  }

  public static class UnknownArgumentsException extends JamonException {
    private static final long serialVersionUID = 2006091701L;

    UnknownArgumentsException(String msg) {
      super(msg);
    }
  }

  private void validateArguments(Map<String, Object> argMap) throws UnknownArgumentsException {
    Set<String> argNames = new HashSet<String>();
    argNames.addAll(argMap.keySet());
    argNames.removeAll(requiredArgNames);
    argNames.removeAll(optionalArgNames);
    if (!argNames.isEmpty()) {
      StringBuilder msg = new StringBuilder("Unknown arguments supplied: ");
      StringUtils.commaJoin(msg, argNames);
      throw new UnknownArgumentsException(msg.toString());
    }
  }

  private Method findSetMethod(String name) {
    Method[] methods = templateClass.getMethods();
    String upperName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    for (int i = 0; i < methods.length; ++i) {
      if (methods[i].getName().equals(upperName)) {
        if (methods[i].getParameterTypes().length == 1) {
          return methods[i];
        }
      }
    }
    return null;
  }

  private void invokeSet(String name, Object value) throws InvalidTemplateException {
    Method setMethod = findSetMethod(name);
    if (setMethod == null) {
      throw new InvalidTemplateException(templateClass.getName() + " has no set method for "
        + name);
    }
    try {
      setMethod.invoke(template, new Object[] { value });
    }
    catch (IllegalAccessException e) {
      throw new InvalidTemplateException(templateClass.getName(), e);
    }
    catch (InvocationTargetException e) {
      Throwable t = e.getTargetException();
      if (t instanceof Error) {
        throw (Error) t;
      }
      else if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      }
      else {
        throw new InvalidTemplateException(templateClass.getName(), t);
      }
    }
  }

  private final Class<?> templateClass;
  private final AbstractTemplateProxy template;
  private final Method renderMethod;
  private final List<String> requiredArgNames;
  private final List<String> optionalArgNames;
}
