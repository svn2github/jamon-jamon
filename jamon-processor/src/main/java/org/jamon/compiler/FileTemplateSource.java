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
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.Properties;
import java.util.StringTokenizer;

import org.jamon.api.TemplateLocation;
import org.jamon.api.TemplateSource;

/**
 * The standard implementation of {@link TemplateSource} which
 * retrieves templates from the filesystem location under a specified
 * root directory. By default, templates are expected to have extens
 * ".jamon"; this can be overridden.
 */
public class FileTemplateSource
    implements TemplateSource
{
    /**
     * Construct a FileTemplateSource
     *
     * @param p_templateSourceDir the source directory
     */
    public FileTemplateSource(String p_templateSourceDir)
    {
        this(new File(p_templateSourceDir));
    }

    /**
     * Construct a FileTemplateSource, using the default extension
     * ".jamon".
     *
     * @param p_templateSourceDir the source directory
     */
    public FileTemplateSource(File p_templateSourceDir)
    {
        this(p_templateSourceDir, "jamon");
    }

    /**
     * Construct a FileTemplateSource, specifying a filename extension
     * for templates. If the supplied extension is null or empty, no
     * extension is expected, otherwise the extension should
     * <emph>NOT</emph> include a leading ".".
     *
     * @param p_templateSourceDir the source directory
     * @param p_extension the filename extension for templates
     */
    public FileTemplateSource(File p_templateSourceDir, String p_extension)
    {
        m_templateSourceDir = p_templateSourceDir;
        m_extension = p_extension == null || p_extension.length() == 0
            ? ""
            : "." + p_extension;
    }

    @Override
    public long lastModified(String p_templatePath)
    {
        return getTemplateFile(p_templatePath).lastModified();
    }

    @Override
    public boolean available(String p_templatePath)
    {
        return getTemplateFile(p_templatePath).exists();
    }

    @Override
    public InputStream getStreamFor(String p_templatePath)
        throws IOException
    {
        return new FileInputStream(getTemplateFile(p_templatePath));
    }

    @Override
    public String getExternalIdentifier(String p_templatePath)
    {
        return getTemplateFile(p_templatePath).getAbsolutePath();
    }

    private File getTemplateFile(String p_templatePath)
    {
        return new File(m_templateSourceDir,
                        templatePathToFilePath(p_templatePath) + m_extension);
    }

    private static String templatePathToFilePath(String p_path)
    {
        if (File.separatorChar == '/')
        {
            return p_path;
        }
        StringTokenizer tokenizer = new StringTokenizer(p_path, "/");
        StringBuilder path = new StringBuilder(p_path.length());
        while (tokenizer.hasMoreTokens())
        {
            path.append(tokenizer.nextToken());
            if (tokenizer.hasMoreTokens())
            {
                path.append(File.separator);
            }
        }
        return path.toString();
    }

    @Override
    public TemplateLocation getTemplateLocation(String p_templatePath)
    {
        return new TemplateFileLocation(getExternalIdentifier(p_templatePath));
    }

    @Override
    public void loadProperties(String p_path, Properties p_properties)
        throws IOException
    {
        File propertiesFile = new File(
            m_templateSourceDir,
            templatePathToFilePath(p_path + "/jamon.properties"));
        if (propertiesFile.canRead())
        {
            FileInputStream fileInputStream =
                new FileInputStream(propertiesFile);
            try
            {
                p_properties.load(fileInputStream);
            }
            finally
            {
                fileInputStream.close();
            }
        }
    }

    private final File m_templateSourceDir;
    private final String m_extension;
}
