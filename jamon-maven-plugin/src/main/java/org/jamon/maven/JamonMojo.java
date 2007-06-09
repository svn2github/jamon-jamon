package org.jamon.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.jamon.compiler.TemplateProcessor;

/**
 * Goal which translates
 *
 * @goal translate
 * @phase generate-sources
     * @execute phase="generate-sources"
 * @requiresDependencyResolution test
 */
public class JamonMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${project.basedir}/src/main/templates"
     */
    private File templateSourceDir;

    /**
     * @parameter expression="${project.build.directory}/tsrc"
     */
    private File templateOutputDir;

    /**
    * @parameter expression="${project}"
    */
    private org.apache.maven.project.MavenProject project;


    private ClassLoader classLoader() throws MojoExecutionException {
      List<URL> urls = new ArrayList<URL>();
      try
      {
        for (Object o : project.getTestClasspathElements()) {
          String s = (String) o;
          File f = new File(s);
          if (f.isDirectory()) 
          {
            urls.add(new URL("file", null, s + '/'));
          }
          else
          {
            urls.add(new URL("file", null, s));
          }
        }
      }
      catch (MalformedURLException e)
      {
        throw new MojoExecutionException("x", e);
      }
      catch (DependencyResolutionRequiredException e)
      {
        throw new MojoExecutionException("x", e);
      }
      getLog().info("URLs are: " + urls);
      return new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
    }
    
    public void execute()
        throws MojoExecutionException
    {
      getLog().info(templateSourceDir.getAbsolutePath());
      getLog().info(templateOutputDir.getAbsolutePath());
      try
      {
        for (Object o : project.getTestClasspathElements()) {
          getLog().info(o.toString());
        }
      }
      catch (DependencyResolutionRequiredException e1)
      {
        throw new MojoExecutionException("dependencies not resolved!", e1);
      }
      List<File> jamonSources = collectSources();
      getLog().info("Translating " + jamonSources.size() + " templates");
      TemplateProcessor processor = new TemplateProcessor(templateOutputDir, templateSourceDir, classLoader());
      for (File f : jamonSources)
      {
        try
        {
          processor.generateSource(f.getPath());
        }
        catch (IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    private List<File> collectSources()
    {
      return accumulateSources(templateSourceDir);
    }

    private List<File> accumulateSources(File p_templateSourceDir)
    {
      List<File> result = new ArrayList<File>();
      for (File f : p_templateSourceDir.listFiles())
      {
        if (f.isDirectory())
        {
          result.addAll(accumulateSources(f));
        }
        else if (f.getName().toLowerCase(Locale.US).endsWith(".jamon"))
        {
          String filePath = f.getPath();
          String basePath = templateSourceDir.getAbsoluteFile().toString(); // FIXME !?

          if (filePath.startsWith(basePath))
          {
              result.add(new File(filePath.substring(basePath.length() + 1)));
          }
          else
          {
            // ??
          }
        }
      }
      return result;
    }
}
