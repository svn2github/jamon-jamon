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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Luis O'Shea, Ian Robertson
 */

package org.jamon.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Location;

import org.apache.tools.ant.types.Path;

import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;

import org.apache.tools.ant.taskdefs.MatchingTask;

import org.jamon.TemplateProcessor;
import org.jamon.JamonParseException;

/**
 * Ant task to convert Jamon templates into Java.
 **/

public class JamonTask
    extends MatchingTask
{

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
            scanDir(srcDir, m_destDir, files);
        }

        m_destDir.mkdirs();
        if (! m_destDir.exists() || ! m_destDir.isDirectory())
        {
            throw new BuildException("Unable to create destination dir "
                                     + m_destDir);
        }

        TemplateProcessor processor =  new TemplateProcessor
            (m_destDir, new File(m_src.toString()));

        try
        {
            for (int i = 0; i < compileList.length; i++)
            {
                processor.generateSource(relativize(basePath, compileList[i]));
            }
        }
        catch (JamonParseException e)
        {
            throw new BuildException(e.getDescription(),
                                     new Location(e.getFileName(),
                                                  e.getLine(),
                                                  e.getColumn()));
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }


    /**
     * Clear the list of files to be compiled and copied..
     */

    protected void resetFileLists()
    {
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
            throw new IllegalArgumentException("Paths must be all absolute");
        }
        String filePath = p_file.getPath();
        if (filePath.startsWith(p_basePath))
        {
            return filePath.substring(p_basePath.length() + 1);
        }
        else
        {
            throw new IllegalArgumentException(p_file + " is not based at " + p_basePath);
        }
    }


    protected File[] compileList = new File[0];

    private String m_package;
    private File m_destDir;
    private Path m_src;
}
