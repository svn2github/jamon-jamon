/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.jamon.compiler.TemplateProcessor;

public abstract class AbstractJamonMojo extends AbstractMojo {
  /**
   * @parameter expression="${project}"
   */
  private MavenProject project;

  /**
   * Sets the granularity in milliseconds of the last modification date for testing whether a source
   * needs recompilation.
   *
   * @parameter expression="${lastModGranularityMs}" default-value="0"
   */
  private int staleMillis;

  /**
   * A list of inclusion filters for the processor.
   *
   * @parameter
   */
  private Set<String> includes = new HashSet<String>();

  /**
   * A list of exclusion filters for the processor.
   *
   * @parameter
   */
  private Set<String> excludes = new HashSet<String>();

  public abstract File getTemplateSourceDir();

  public abstract File getTemplateOutputDir();

  private ClassLoader classLoader() throws MojoExecutionException {
    List<URL> urls = new ArrayList<URL>();
    try {
      for (Object o : project.getTestClasspathElements()) {
        String s = (String) o;
        File f = new File(s);
        try {
          if (f.isDirectory() || !f.exists()) {
            urls.add(new URL("file", null, s + '/'));
          }
          else {
            urls.add(new URL("file", null, s));
          }
        }
        catch (MalformedURLException e) {
          throw new MojoExecutionException("Unable to turn element" + s + " to URL", e);
        }
      }
    }
    catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException("test dependencies not resolved", e);
    }
    getLog().debug("URLs are: " + urls);
    return new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader
        .getSystemClassLoader());
  }

  protected void doExecute() throws MojoExecutionException {
    List<File> jamonSources = collectSources();
    if (jamonSources.size() > 0) {
      getLog().info(
        "Translating " + jamonSources.size() + " templates from "
          + getTemplateSourceDir().getPath() + " to " + getTemplateOutputDir().getPath());
      TemplateProcessor processor =
        new TemplateProcessor(getTemplateOutputDir(), getTemplateSourceDir(), classLoader());
      for (File f : jamonSources) {
        try {
          processor.generateSource(f.getPath());
        }
        catch (IOException e) {
          throw new MojoExecutionException("unable to translate template", e);
        }
      }
    }
  }

  private List<File> collectSources() throws MojoExecutionException {
    return accumulateSources(getTemplateSourceDir());
  }

  private List<File> accumulateSources(File templateSourceDir) throws MojoExecutionException {
    if (!templateSourceDir.exists()) {
      throw new MojoExecutionException(
        "templateSourceDir " + templateSourceDir + " does not exist");
    }
    if (!templateSourceDir.isDirectory()) {
      throw new MojoExecutionException(
        "templateSourceDir " + templateSourceDir + " is not a directory");
    }
    SourceInclusionScanner scanner = getSourceInclusionScanner(staleMillis);
    scanner.addSourceMapping(new SuffixMapping(".jamon", ".java"));
    scanner.addSourceMapping(new SuffixMapping(".jamon", "Impl.java"));

    final Set<File> staleFiles = new LinkedHashSet<File>();

    if (templateSourceDir.isDirectory()) {
      try {
        @SuppressWarnings("unchecked")
        Set<File> includedSources = scanner.getIncludedSources(templateSourceDir,
          getTemplateOutputDir());
        staleFiles.addAll(includedSources);
      }
      catch (InclusionScanException e) {
        throw new MojoExecutionException(
          "Error scanning source root: \'" + templateSourceDir.getPath()
          + "\' for stale files to recompile.",
          e);
      }
    }

    return relativizeFiles(staleFiles);
  }

  /**
   * Trim the the root path from file paths.
   *
   * @param files
   * @return a list of File objects which are relative to {@link #getTemplateSourceDir()}.
   */
  private List<File> relativizeFiles(final Set<File> files) {
    final List<File> result = new ArrayList<File>();
    for (File file : files) {
      URI templateSourceUri = getTemplateSourceDir().toURI();
      result.add(new File(templateSourceUri.relativize(file.toURI()).getPath()));
    }
    return result;
  }

  protected MavenProject getProject() {
    return project;
  }

  private SourceInclusionScanner getSourceInclusionScanner(int staleMillis) {
    return new StaleSourceScanner(staleMillis, includes.isEmpty()
        ? Collections.singleton("**/*.jamon")
        : includes, excludes);
  }
}
