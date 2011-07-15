package org.jamon.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Translates Jamon test templates into Java files.
 *
 * @goal translate-tests
 * @phase generate-test-sources
 * @threadSafe
 * @requiresDependencyResolution test
 */
public class JamonTestMojo
    extends AbstractJamonMojo
{
    /**
     * @parameter expression="${project.basedir}/src/test/templates"
     */
    private File templateSourceDir;

    /**
     * @parameter expression="${project.build.directory}/generated-test-sources/jamon"
     */
    private File templateOutputDir;

    @Override
    public File getTemplateOutputDir()
    {
      return templateOutputDir;
    }

    @Override
    public File getTemplateSourceDir()
    {
      return templateSourceDir;
    }

    public void execute()
        throws MojoExecutionException
    {
      doExecute();
      getProject().addTestCompileSourceRoot(templateOutputDir.getAbsolutePath());
    }
}
