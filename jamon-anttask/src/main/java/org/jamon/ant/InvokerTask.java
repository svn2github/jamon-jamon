/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.jamon.BasicTemplateManager;
import org.jamon.TemplateManager;
import org.jamon.api.ParserError;
import org.jamon.compiler.InvokerTool;
import org.jamon.compiler.JamonException;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.compiler.RecompilingTemplateManager;
import org.jamon.compiler.TemplateInspector;
import org.jamon.compiler.TemplateInspector.InvalidTemplateException;

/**
 * Ant task to reflectively invoke templates.
 **/

public class InvokerTask extends Task {
  public InvokerTask() {
    recompilingManagerData = new RecompilingTemplateManager.Data();
  }

  private Writer computeWriter() throws IOException {
    if (outputPropertyName != null) {
      return new StringWriter();
    }
    else if (output != null) {
      File parent = output.getAbsoluteFile().getParentFile();
      if (parent != null) {
        // FIXME: should we check for failure here
        // or let it fall through?
        parent.mkdirs();
      }
      return new FileWriter(output);
    }
    else {
      return new OutputStreamWriter(System.out);
    }
  }

  @Override
  public void execute() throws BuildException {
    Properties sysprops = (Properties) System.getProperties().clone();
    try {
      for (Environment.Variable var : this.sysprops) {
        System.setProperty(var.getKey(), var.getValue());
      }
      Writer writer = computeWriter();
      TemplateManager manager = dynamicRecompilation
          ? new RecompilingTemplateManager(recompilingManagerData)
          : (TemplateManager) new BasicTemplateManager(classLoader);

      TemplateInspector inspector = new TemplateInspector(manager, path);

      convertArguments(inspector);

      inspector.render(writer, args);
      if (outputPropertyName != null) {
        getProject().setProperty(outputPropertyName, writer.toString());
      }
    }
    catch (InvalidTemplateException e) {
      throw new BuildException(e);
    }
    catch (JamonException e) {
      throw new BuildException(e);
    }
    catch (ParserErrorsImpl e) {
      e.printErrors(System.err); // FIXME - is this the right thing to do?
      if (!e.getErrors().isEmpty()) {
        ParserError error = e.getErrors().get(0);
        throw new BuildException(error.getMessage(), new JamonLocation(error.getLocation()));
      }
      else {
        throw new BuildException("Jamon translation failed");
      }
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
    finally {
      System.setProperties(sysprops);
    }
  }

  private void convertArguments(TemplateInspector inspector)
  throws InvokerTool.TemplateArgumentException {
    InvokerTool.ObjectParser parser = new InvokerTool.DefaultObjectParser();
    for (Map.Entry<String, Object> entry : args.entrySet()) {
      entry.setValue(
        parser.parseObject(inspector.getArgumentType(entry.getKey()), (String) entry.getValue()));
    }
  }

  private static final String SINGLE_OUTPUT_ERROR_MESSAGE =
    "Can't specify both output file and output property name";

  public void setProperty(String outputPropertyName) {
    if (output != null) {
      throw new BuildException(SINGLE_OUTPUT_ERROR_MESSAGE);
    }
    this.outputPropertyName = outputPropertyName;
  }

  public void setCompiler(String javac) {
    if (javac != null && javac.length() > 0) {
      recompilingManagerData.setJavaCompiler(javac);
    }
  }

  public void addConfiguredClasspath(Path classpath) throws IOException {
    doSetClasspath(classpath);
  }

  public void setClasspathref(Reference classpathid) throws IOException {
    Path path = (Path) classpathid.getReferencedObject();
    doSetClasspath(path);
  }

  public void setClasspath(Path classpath) throws IOException {
    doSetClasspath(classpath);
  }

  private void doSetClasspath(Path classpath) throws IOException {
    String[] paths = classpath.list();
    URL[] urls = new URL[paths.length];
    for (int i = 0; i < urls.length; ++i) {
      urls[i] = new URL("file", null, paths[i] + (new File(paths[i]).isDirectory()
          ? "/"
          : ""));
    }
    classLoader = new URLClassLoader(urls, getClass().getClassLoader());
    recompilingManagerData.setClassLoader(classLoader);
    recompilingManagerData.setClasspath(classpath.toString());
  }

  public void setWorkDir(File workDir) {
    recompilingManagerData.setWorkDir(workDir.getAbsolutePath());
  }

  public void setSourceDir(File sourceDir) {
    recompilingManagerData.setSourceDir(sourceDir.getAbsolutePath());
  }

  public void setOutput(File output) {
    if (outputPropertyName != null) {
      throw new BuildException(SINGLE_OUTPUT_ERROR_MESSAGE);
    }
    this.output = output;
  }

  public void setTemplate(String path) {
    this.path = path;
  }

  public void setDynamicRecompilation(boolean dynamicRecompilation) {
    this.dynamicRecompilation = dynamicRecompilation;
  }

  private final RecompilingTemplateManager.Data recompilingManagerData;

  private boolean dynamicRecompilation = true;

  private String path;

  private HashMap<String, Object> args = new HashMap<String, Object>();

  private Collection<Environment.Variable> sysprops = new HashSet<Environment.Variable>();

  private File output;

  private String outputPropertyName;

  private ClassLoader classLoader;

  public void addSysproperty(Environment.Variable property) {
    sysprops.add(property);
  }

  public void addConfiguredArg(Arg arg) {
    args.put(arg.name, arg.value);
  }

  public static class Arg {
    public void setName(String name) {
      this.name = name;
    }

    public void setValue(String value) {
      this.value = value;
    }

    private String name;

    private String value;
  }
}
