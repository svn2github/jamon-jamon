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

package org.jamon;

import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;
import java.io.File;
import java.util.StringTokenizer;

/*
this takes the place of some of TemplateDescriber.

*/
public class FileTemplateSource
    implements TemplateSource
{
    public FileTemplateSource(String p_templateSourceDir)
    {
        this(new File(p_templateSourceDir));
    }

    public FileTemplateSource(File p_templateSourceDir)
    {
        m_templateSourceDir = p_templateSourceDir;
    }

    private final File m_templateSourceDir;

    public long lastModified(String p_templatePath)
        throws IOException
    {
        return getTemplateFile(p_templatePath).lastModified();
    }

    public boolean available(String p_templatePath)
        throws IOException
    {
        return getTemplateFile(p_templatePath).exists();
    }

    public Reader getReaderFor(String p_templatePath)
        throws IOException
    {
        return new FileReader(getTemplateFile(p_templatePath));
    }

    public String getExternalIdentifier(String p_templatePath)
    {
        return getTemplateFile(p_templatePath).getAbsolutePath();
    }

    private File getTemplateFile(String p_templatePath)
    {
        String filePath = templatePathToFilePath(p_templatePath);
        File file = new File(m_templateSourceDir,
                             filePath + ".jamon");
        if (! file.exists())
        {
            file = new File(m_templateSourceDir,filePath + ".jam");
        }
        if (! file.exists())
        {
            file = new File(m_templateSourceDir,filePath);
        }
        return file;
    }

    private String templatePathToFilePath(String p_path)
    {
        StringTokenizer tokenizer = new StringTokenizer(p_path, "/");
        StringBuffer path = new StringBuffer(p_path.length());
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
}
