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

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class InvokerTool {

  /**
   * An <code>ObjectParser</code> describes how to convert string values to objects.
   */
  public static interface ObjectParser {
    Object parseObject(Class<?> p_type, String p_value) throws TemplateArgumentException;
  }

  public static class DefaultObjectParser implements ObjectParser {
    @Override
    public Object parseObject(Class<?> type, String string) throws TemplateArgumentException {
      try {
        if (string == null) {
          if (type.isPrimitive()) {
            throw new TemplateArgumentException("primitive types cannot be null");
          }
          else {
            return null;
          }
        }
        else if (type == String.class) {
          return string;
        }
        else if (type == Boolean.class || type == Boolean.TYPE) {
          return Boolean.valueOf(string);
        }
        else if (type == Integer.class || type == Integer.TYPE) {
          return Integer.valueOf(string);
        }
        else if (type == Float.class || type == Float.TYPE) {
          return Float.valueOf(string);
        }
        else if (type == Double.class || type == Double.TYPE) {
          return Double.valueOf(string);
        }
        else if (type == Short.class || type == Short.TYPE) {
          return Short.valueOf(string);
        }
        else if (type == Byte.class || type == Byte.TYPE) {
          return Byte.valueOf(string);
        }
        else {
          return type.getConstructor(new Class[] { String.class }).newInstance(
            new Object[] { string });
        }
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new TemplateArgumentException(e);
      }
    }
  }

  public static class TemplateArgumentException extends JamonException {
    public TemplateArgumentException(Throwable t) {
      super(t);
    }

    public TemplateArgumentException(String msg) {
      super(msg);
    }

    private static final long serialVersionUID = 2006091701L;
  }

  public static class UsageException extends Exception {
    @Override
    public String toString() {
      return "java " + InvokerTool.class.getName() + " [-o outputfile] "
        + " [-s templatesourcedir]" + " [-w workdir]" + " template-path [[arg1=val1] ...]";
    }

    private static final long serialVersionUID = 2006091701L;
  }

  public InvokerTool(ObjectParser objectParser) {
    this.objectParser = objectParser;
  }

  private final ObjectParser objectParser;

  public InvokerTool() {
    this(new DefaultObjectParser());
  }

  private void parseArgString(
    TemplateInspector inspector, Map<String, Object> argMap, String arg)
  throws UsageException, TemplateArgumentException {
    int i = arg.indexOf("=");
    if (i <= 0) {
      throw new UsageException();
    }
    String name = arg.substring(0, i);
    argMap.put(name, objectParser.parseObject(inspector.getArgumentType(name), arg
        .substring(i + 1)));
  }

  protected void invoke(String[] args) throws UsageException,
    IOException,
    TemplateArgumentException,
    TemplateInspector.UnknownArgumentsException,
    TemplateInspector.InvalidTemplateException {
    int a = 0;
    RecompilingTemplateManager.Data data = new RecompilingTemplateManager.Data();
    String outFile = null;
    while (a < args.length && args[a].startsWith("-")) {
      if (args[a].startsWith("--workdir=")) {
        data.setWorkDir(args[a].substring(10));
      }
      else if (args[a].equals("-w")) {
        a++;
        if (a < args.length) {
          data.setWorkDir(args[a]);
        }
        else {
          throw new UsageException();
        }
      }
      else if (args[a].startsWith("--srcdir=")) {
        data.setSourceDir(args[a].substring(9));
      }
      else if (args[a].equals("-s")) {
        a++;
        if (a < args.length) {
          data.setSourceDir(args[a]);
        }
        else {
          throw new UsageException();
        }
      }
      else if (args[a].startsWith("--output=")) {
        outFile = args[a].substring(9);
      }
      else if (args[a].equals("-o")) {
        a++;
        if (a < args.length) {
          outFile = args[a];
        }
        else {
          throw new UsageException();
        }
      }
      else {
        throw new UsageException();
      }
      a++;
    }
    if (a >= args.length) {
      throw new UsageException();
    }

    String templateName = args[a++];

    TemplateInspector inspector =
      new TemplateInspector(new RecompilingTemplateManager(data), templateName);

    HashMap<String, Object> argMap = new HashMap<String, Object>();
    while (a < args.length) {
      parseArgString(inspector, argMap, args[a++]);
    }

    Writer writer = outFile == null
        ? new OutputStreamWriter(System.out)
        : new FileWriter(outFile);

    inspector.render(writer, argMap);
  }

  public static void main(String[] args) throws Exception {
    try {
      new InvokerTool().invoke(args);
    }
    catch (UsageException e) {
      System.err.println("Usage: " + e);
    }
  }
}
