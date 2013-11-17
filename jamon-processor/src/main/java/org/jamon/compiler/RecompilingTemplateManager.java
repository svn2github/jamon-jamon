/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jamon.AbstractTemplateImpl;
import org.jamon.AbstractTemplateManager;
import org.jamon.AbstractTemplateProxy;
import org.jamon.IdentityTemplateReplacer;
import org.jamon.TemplateManager;
import org.jamon.TemplateReplacer;
import org.jamon.AbstractTemplateProxy.Intf;
import org.jamon.annotations.Template;
import org.jamon.api.SourceGenerator;
import org.jamon.api.TemplateSource;
import org.jamon.codegen.Analyzer;
import org.jamon.codegen.ImplGenerator;
import org.jamon.codegen.ProxyGenerator;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateUnit;
import org.jamon.util.JavaCompiler;
import org.jamon.util.JavaCompilerFactory;
import org.jamon.util.StringUtils;
import org.jamon.util.WorkDirClassLoader;

/**
 * An implementation of the {@link TemplateManager} interface which supports dynamic regeneration
 * and recompilation of templates as they are changed, much as JSP does.
 * <code>RecompilingTemplateManager</code> instances are thread-safe. In your applications, you
 * generally want exactly one instance of a RecompilingTemplateManager (i.e. a singleton).
 * Configuration of a <code>RecompilingTemplateManager</code> occurs only at construction time, and
 * is determined by the <code>RecompilingTemplateManager.Data</code> object passed to the
 * constructor. The properties on the <code>Data</code> are:
 * <ul>
 * <li><b>setSourceDir</b> - determines where the <code>RecompilingTemplateManager</code> looks for
 * template source files. Default is the current directory, which is most likely unsuitable for most
 * situations.
 * <li><b>setWorkDir</b> - determines where the generated Java source files corresponding to
 * templates are placed. Default is uniquely generated subdirectory under the directory specified by
 * the system property <tt>java.io.tmpdir</tt>.
 * <li><b>setJavaCompiler</b> - determines what program to execute to compile the generated Java
 * source files. Default is <tt>bin/javac</tt> (<tt>Commands/javac</tt> on MacOS) under the
 * directory specified by the system property <tt>java.home</tt>.
 * <li><b>setClasspath</b> - used to specify additional components to prepend to the classpath when
 * compiling generated Java source files. Default is null.
 * <li><b>setClassLoader</b> - used to set the class loader explicitly. Default is use the class
 * loader of the <code>RecompilingTemplateManager</code> instance.
 * </ul>
 */

public class RecompilingTemplateManager extends AbstractTemplateManager {
  public static class Data {
    public Data setSourceDir(String sourceDir) {
      this.sourceDir = sourceDir;
      return this;
    }

    public String getSourceDir() {
      return sourceDir;
    }

    private String sourceDir;

    public Data setTemplateSource(TemplateSource templateSource) {
      this.templateSource = templateSource;
      return this;
    }

    public TemplateSource getTemplateSource() {
      return templateSource;
    }

    private TemplateSource templateSource;

    public Data setWorkDir(String workDir) {
      this.workDir = workDir;
      return this;
    }

    public String getWorkDir() {
      return workDir;
    }

    private String workDir;

    public Data setJavaCompiler(String javaCompiler) {
      this.javaCompiler = javaCompiler;
      return this;
    }

    public String getJavaCompiler() {
      return javaCompiler;
    }

    private String javaCompiler;

    public Data setClasspath(String classpath) {
      this.classpath = classpath;
      return this;
    }

    public String getClasspath() {
      return classpath;
    }

    private String classpath;

    public Data setClassLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
      return this;
    }

    public ClassLoader getClassLoader() {
      return classLoader;
    }

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public Data setTemplateReplacer(TemplateReplacer templateReplacer) {
      this.templateReplacer = templateReplacer;
      return this;
    }

    public TemplateReplacer getTemplateReplacer() {
      return templateReplacer;
    }

    private TemplateReplacer templateReplacer;
  }

  public RecompilingTemplateManager() {
    this(new Data());
  }

  public RecompilingTemplateManager(Data data) {
    super(data.getTemplateReplacer() == null
        ? IdentityTemplateReplacer.INSTANCE
        : data.getTemplateReplacer());

    classLoader = data.classLoader == null
        ? getClass().getClassLoader()
        : data.classLoader;

    workDir = data.workDir == null
        ? getDefaultWorkDir()
        : data.workDir;
    javaCompiler = JavaCompilerFactory.makeCompiler(data, workDir, classLoader);
    if (TRACE) {
      trace("Java Compiler: " + javaCompiler.getClass().getSimpleName());
    }
    if (data.templateSource != null) {
      templateSource = data.templateSource;
    }
    else {
      templateSource = new FileTemplateSource(data.sourceDir == null
          ? System.getProperty("user.dir")
          : data.sourceDir);
    }

    loader = AccessController.doPrivileged(new PrivilegedAction<WorkDirClassLoader>() {
      @Override
      public WorkDirClassLoader run() {
        return new WorkDirClassLoader(classLoader, workDir);
      }
    });
  }

  @Override
  protected Intf constructImplFromReplacedProxy(AbstractTemplateProxy replacedProxy) {
    return replacedProxy.constructImpl(getImplClass(replacedProxy.getClass()));
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
      // need to do this first to check dependencies if so enabled
      return getProxyClass(path).getConstructor(new Class[] { TemplateManager.class })
          .newInstance(new Object[] { this });
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void trace(String p_message) {
    System.err.println(p_message);
  }

  private static String getDefaultWorkDir() {
    File workDir = new File(System.getProperty("java.io.tmpdir"), "jamon"
      + (new java.util.Random().nextInt(100000000)) + ".tmp");
    if (!workDir.mkdirs()) {
      throw new RuntimeException("Unable to create default work directory " + workDir);
    }
    workDir.deleteOnExit();
    return workDir.toString();
  }

  private Class<? extends AbstractTemplateImpl> getImplClass(
    Class<? extends AbstractTemplateProxy> proxyClass) {
    return getTemplateClass(
      StringUtils.classToTemplatePath(proxyClass), proxyClass.getName() + "Impl")
      .asSubclass(AbstractTemplateImpl.class);
  }

  private Class<? extends AbstractTemplateProxy> getProxyClass(String path) {
    return getTemplateClass(
      path, StringUtils.templatePathToClassName(path)).asSubclass(AbstractTemplateProxy.class);
  }

  private Class<?> getTemplateClass(String path, String className) {
    try {
      try {
        ensureUpToDate(path, new TemplateDescriber(templateSource, loader));
        return loader.loadClass(className);
      }
      catch (ClassNotFoundException e) {
        if (!templateSource.available(path)) {
          throw new RuntimeException("The template at path " + path + " could not be found");
        }
        else {
          throw new RuntimeException(e);
        }
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final TemplateSource templateSource;
  private final String workDir;
  private final ClassLoader classLoader;
  private final JavaCompiler javaCompiler;
  private final WorkDirClassLoader loader;

  private String prefix() {
    return workDir;
  }

  private String javaImpl(String path) {
    return prefix() + path + "Impl.java";
  }

  private String classImpl(String path) {
    return prefix() + path + "Impl.class";
  }

  private String javaIntf(String p_path) {
    return prefix() + p_path + ".java";
  }

  private synchronized void ensureUpToDate(String startingPath, TemplateDescriber describer)
  throws IOException {
    Collection<String> seen = new HashSet<String>();
    Collection<String> outOfDateJavaFiles = new HashSet<String>();
    List<String> workQueue = new LinkedList<String>();
    workQueue.add(startingPath);

    while (!workQueue.isEmpty()) {
      String path = workQueue.remove(0);
      if (TRACE) {
        trace("processing " + path);
      }
      seen.add(path);

      if (!templateSource.available(path)) {
        if (TRACE) {
          trace(path + " source not found; assume class exists");
        }
        continue;
      }

      boolean intfGenerated = false;
      long modTime = templateSource.lastModified(path);

      String intfFileName = javaIntf(path);
      File intfFile = new File(intfFileName);
      if (intfFile.lastModified() < modTime) {
        TemplateUnit templateUnit = new Analyzer(path, describer).analyze();
        String signature = templateUnit.getSignature();
        if (isIntfChanged(path, signature, classLoader)) {
          if (isIntfChanged(path, signature, loader)) {
            generateIntf(path, describer, templateUnit);
            outOfDateJavaFiles.add(intfFileName);
            intfGenerated = true;
          }
        }
        else {
          intfFile.delete();
          deleteClassFilesFor(path);
        }

      }

      File jm = new File(javaImpl(path));
      long ts = jm.lastModified();
      if (jm.lastModified() < modTime) {
        dependencyCache.put(path, new DependencyEntry(generateImpl(path, describer)));
        ts = System.currentTimeMillis();
      }

      if (new File(classImpl(path)).lastModified() < ts || intfGenerated) {
        outOfDateJavaFiles.add(javaImpl(path));
      }

      DependencyEntry d = dependencyCache.get(path);
      if (d == null || d.lastUpdated() < modTime) {
        d = new DependencyEntry(computeDependencies(path, describer));
        dependencyCache.put(path, d);
      }
      for (String dp : d.getDependencies()) {
        if (!seen.contains(dp)) {
          workQueue.add(dp);
        }
      }
    }

    if (!outOfDateJavaFiles.isEmpty()) {
      String errors = compile(outOfDateJavaFiles);
      loader.invalidate();
      if (errors != null) {
        throw new TemplateCompilationException(errors);
      }
    }
  }

  private void deleteClassFilesFor(String path) {
    int i = path.lastIndexOf('/');
    String templateName = i < 0
        ? path
        : path.substring(i + 1);
    File dir = new File(new File(workDir), StringUtils.templatePathToFileDir(path));
    String[] files = dir.list();
    if (files != null) {
      for (int j = 0; j < files.length; ++j) {
        if (StringUtils.isGeneratedClassFilename(templateName, files[j])) {
          new File(dir, files[j]).delete();
          // FIXME: error checking?
        }
      }
    }
  }

  private File getWriteableFile(String filename) {
    File file = new File(filename);
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }
    return file;
  }

  private void generateSource(String filename, SourceGenerator sourceGenerator) throws IOException {
    File javaFile = getWriteableFile(filename);
    FileOutputStream out = new FileOutputStream(javaFile);
    try {
      sourceGenerator.generateSource(out);
      out.close();
    }
    catch (IOException e) {
      out.close();
      javaFile.delete();
      throw e;
    }
  }

  /**
   * @return dependencies
   */
  private Collection<String> generateImpl(String path, TemplateDescriber describer)
  throws IOException {
    if (TRACE) {
      trace("generating impl for " + path);
    }

    TemplateUnit templateUnit = new Analyzer(path, describer).analyze();

    generateSource(javaImpl(path), new ImplGenerator(describer, templateUnit));
    return templateUnit.getTemplateDependencies();
  }

  private void generateIntf(String path, TemplateDescriber describer, TemplateUnit templateUnit)
  throws IOException {
    if (TRACE) {
      trace("generating intf for " + path);
    }

    generateSource(javaIntf(path), new ProxyGenerator(describer, templateUnit));
  }

  private String getIntfSignatureFromClass(String path, ClassLoader loader) {
    if (TRACE) {
      trace("Looking for signature of " + StringUtils.templatePathToClassName(path));
    }
    try {
      return loader.loadClass(StringUtils.templatePathToClassName(path)).getAnnotation(
        Template.class).signature();
    }
    catch (ClassNotFoundException e) {
      return null;
    }
  }

  private boolean isIntfChanged(String path, String signature, ClassLoader loader) {
    return !signature.equals(getIntfSignatureFromClass(path, loader));
  }

  private String compile(Collection<String> sourceFiles) {
    if (sourceFiles.isEmpty()) {
      return null;
    }

    StringBuilder buf = new StringBuilder();
    buf.append("compiling: ");
    StringUtils.commaJoin(buf, sourceFiles);
    if (TRACE) {
      trace(buf.toString());
    }
    return javaCompiler.compile(sourceFiles.toArray(new String[sourceFiles.size()]));
  }

  private Collection<String> computeDependencies(String path, TemplateDescriber describer)
  throws IOException {
    if (TRACE) {
      trace("computing dependencies for " + path);
    }

    return new Analyzer(path, describer).analyze().getTemplateDependencies();
  }

  private Map<String, DependencyEntry> dependencyCache = new HashMap<String, DependencyEntry>();

  private static class DependencyEntry {
    DependencyEntry(Collection<String> dependencies) {
      this.dependencies = dependencies;
      lastUpdated = System.currentTimeMillis();
    }

    Collection<String> dependencies;

    long lastUpdated;

    Collection<String> getDependencies() {
      return dependencies;
    }

    long lastUpdated() {
      return lastUpdated;
    }
  }

  // FIXME - use JavaLogging.
  public static final boolean TRACE = Boolean.valueOf(
    System.getProperty(RecompilingTemplateManager.class.getName() + ".trace")).booleanValue();
}
