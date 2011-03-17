package org.jamon.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Translates Jamon templates into Java files.
 *
 * @goal translate
 * @phase generate-sources
 * @threadSafe
 * @requiresDependencyResolution runtime
 */
public class JamonMojo
    extends AbstractJamonMojo
{
    /**
     * @parameter expression="${project.basedir}/src/main/templates"
     */
    private File templateSourceDir;

    /**
     * @parameter expression="${project.build.directory}/generated-sources/jamon"
     */
    private File templateOutputDir;

    @Override
    protected File templateOutputDir()
    {
      return templateOutputDir;
    }

    @Override
    protected File templateSourceDir()
    {
      return templateSourceDir;
    }

    public void execute()
        throws MojoExecutionException
    {
      doExecute();
      getProject().addCompileSourceRoot(templateOutputDir.getAbsolutePath());
    }
}
