package org.jamon.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;

import org.apache.tools.ant.types.Path;

import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;

import org.apache.tools.ant.taskdefs.MatchingTask;

import org.jamon.TemplateGenerator;
import org.jamon.StringUtils;
import org.jamon.parser.ParserException;

/************************************************************************************
 *
 * Ant task to convert Jamon templates into Java.
 *
 ************************************************************************************/

public class JamonTask
    extends MatchingTask
{

    public void setPackage(String p_package)
    {
        m_package = p_package;
    }


    public void setDestdir(File p_destDir)
    {
        m_destDir = p_destDir;
    }


    public Path createSrc()
    {
        if (m_src == null) 
        {
            m_src = new Path(project);
        }
        return m_src.createPath();
    }


    public void execute() 
        throws BuildException 
    {
        log("package = " + m_package);
        log("destDir = " + m_destDir);
        log("src = " + m_src);
        String[] paths = m_src.list();
        // FIXME: This probably needs some coordination with the core
        // Jamon project.
        if (paths.length != 1)
        {
            throw new BuildException("Must have exactly one src!", location);
        }
        String basePath = paths[0];
        log("basepath = " + basePath);

        // Copied from org.apache.tools.ant.taskdefs.Javac below

        // first off, make sure that we've got a srcdir

        if (m_src == null) 
        {
            throw new BuildException("srcdir attribute must be set!", location);
        }
        String [] list = m_src.list();
        if (list.length == 0) 
        {
            throw new BuildException("srcdir attribute must be set!", location);
        }

        if (m_destDir != null && !m_destDir.isDirectory()) 
        {
            throw new BuildException("destination directory \"" + 
                                     m_destDir + 
                                     "\" does not exist or is not a directory", 
                                     location);
        }

        // scan source directories and dest directory to build up
        // compile lists
        File destDir = new File(m_destDir, StringUtils.classNameToPath(m_package));
        log("effective destDir = " + destDir);
        resetFileLists();
        for (int i = 0; i < list.length; i++) 
        {
            File srcDir = (File)project.resolveFile(list[i]);
            if (!srcDir.exists()) 
            {
                throw new BuildException("srcdir \"" + srcDir.getPath() + "\" does not exist!", location);
            }

            DirectoryScanner ds = this.getDirectoryScanner(srcDir);

            String[] files = ds.getIncludedFiles();
            scanDir(srcDir, destDir, files);
        }

        String[] relativeFilenames = new String[compileList.length];
        String[] absoluteFilenames = new String[compileList.length];
        for (int i = 0; i < compileList.length; i++)
        {
            relativeFilenames[i] = relativize(basePath, compileList[i]);
            absoluteFilenames[i] = compileList[i].getPath();
        }

        try
        {
            TemplateGenerator.generateInterfaces(m_destDir,
                                                 m_package,
                                                 relativeFilenames,
                                                 absoluteFilenames);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }


    /**
     * Clear the list of files to be compiled and copied..
     */

    protected void resetFileLists() {
        compileList = new File[0];
    }


    /**
     * Scans the directory looking for source files to be compiled.
     * The results are returned in the class variable compileList
     */

    protected void scanDir(File srcDir, File destDir, String files[]) 
    {
        GlobPatternMapper m = new GlobPatternMapper();
        m.setFrom("*");
        m.setTo("*.java");
        SourceFileScanner sfs = new SourceFileScanner(this);
        File[] newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);

        if (newFiles.length > 0) {
            File[] newCompileList = new File[compileList.length +
                newFiles.length];
            System.arraycopy(compileList, 0, newCompileList, 0,
                    compileList.length);
            System.arraycopy(newFiles, 0, newCompileList,
                    compileList.length, newFiles.length);
            compileList = newCompileList;
        }
    }


    private static String relativize(String p_basePath, File p_file)
    {
        if (!p_file.isAbsolute())
        {
            throw new RuntimeException("Paths must be all absolute");
        }
        String filePath = p_file.getPath();
        if (filePath.startsWith(p_basePath))
        {
            return filePath.substring(p_basePath.length() + 1);
        }
        else
        {
            throw new RuntimeException(p_file + " is not based at " + p_basePath);
        }
    }


    protected File[] compileList = new File[0];

    private String m_package;
    private File m_destDir;
    private Path m_src;

}
