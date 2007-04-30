package org.jamon.nodegen;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Generate AST nodes.
 *
 * @goal generate-ast-nodes
 * @phase generate-source
 */
public class NodeGenMojo extends AbstractMojo
{
    public void execute() throws MojoExecutionException
    {
        getLog().info("Hello, world.");
    }

    /**
     * @parameter
     * @required
     */
    private File nodeDescriptionFile;
    /**
     * @parameter
     * @required
     */
    private File destinationDirectory;
}
