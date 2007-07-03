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
import org.apache.maven.project.MavenProject;
import org.jamon.compiler.TemplateProcessor;

public abstract class AbstractJamonMojo
    extends AbstractMojo
{
    /**
    * @parameter expression="${project}"
    */
    private MavenProject project;

    protected abstract File templateSourceDir();
    protected abstract File templateOutputDir();

    private ClassLoader classLoader() throws MojoExecutionException 
    {
      List<URL> urls = new ArrayList<URL>();
      try
      {
        for (Object o : project.getTestClasspathElements()) 
        {
          String s = (String) o;
          File f = new File(s);
          try
          {
            if (f.isDirectory() || ! f.exists()) 
            {
              urls.add(new URL("file", null, s + '/'));
            }
            else
            {
              urls.add(new URL("file", null, s));
            }
          }
          catch (MalformedURLException e)
          {
            throw new MojoExecutionException("Unable to turn element"
                                             + s + " to URL", e);
          }
        }
      }
      catch (DependencyResolutionRequiredException e)
      {
        throw new MojoExecutionException("test dependencies not resolved", e);
      }
      getLog().debug("URLs are: " + urls);
      return new URLClassLoader(urls.toArray(new URL[urls.size()]),
                                ClassLoader.getSystemClassLoader());
    }
    
    protected void doExecute()
        throws MojoExecutionException
    {
      List<File> jamonSources = collectSources();
      getLog().info("Translating "
                    + jamonSources.size() 
                    + " templates from "
                    + templateSourceDir().getPath() 
                    + " to " 
                    + templateOutputDir().getPath());
      TemplateProcessor processor =
           new TemplateProcessor(templateOutputDir(),
                                 templateSourceDir(),
                                 classLoader());
      for (File f : jamonSources)
      {
        try
        {
          processor.generateSource(f.getPath());
        }
        catch (IOException e)
        {
          throw new MojoExecutionException("unable to translate template", e);
        }
      }
    }

    private List<File> collectSources()
    {
      return accumulateSources(templateSourceDir());
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
           // FIXME !?
          String basePath = templateSourceDir().getAbsoluteFile().toString();
          result.add(new File(filePath.substring(basePath.length() + 1)));
        }
      }
      return result;
    }
    
    protected MavenProject getProject()
    { 
      return project;
    }
}
