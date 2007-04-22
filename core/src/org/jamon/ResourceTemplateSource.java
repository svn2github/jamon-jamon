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
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.jamon.api.TemplateLocation;

/**
 * An implementation of {@link TemplateSource} which retrieves
 * templates from Java resources.
 */
public class ResourceTemplateSource
    implements TemplateSource
{
    public ResourceTemplateSource(ClassLoader p_classLoader, String p_templateSourceDir)
    {
        this(p_classLoader, p_templateSourceDir, "jamon");
    }

    /**
     * Construct a ResourceTemplateSource, specifying a filename extension
     * for templates. If the supplied extension is null or empty, no
     * extension is expected, otherwise the extension should
     * <emph>NOT</emph> include a leading ".".
     *
     * @param p_classLoader the classloader to use to load resources
     * @param p_templateSourceDir the source directory
     * @param p_extension the filename extension for templates
     */
    public ResourceTemplateSource(ClassLoader p_classLoader, String p_templateSourceDir, String p_extension)
    {
        m_classLoader = p_classLoader;
        m_templateSourceDir = p_templateSourceDir;
        m_extension = p_extension == null || p_extension.length() == 0
            ? ""
            : "." + p_extension;
    }

    public long lastModified(String p_templatePath)
        throws IOException
    {
        return getTemplate(p_templatePath).getLastModified();
    }

    public boolean available(String p_templatePath)
    {
        // FIXME: is this the way to implement this?
        return getUrl(p_templatePath) != null;
    }

    public InputStream getStreamFor(String p_templatePath)
        throws IOException
    {
        return getTemplate(p_templatePath).getInputStream();
    }

    public String getExternalIdentifier(String p_templatePath)
    {
        // return getUrl(p_templatePath).toExternalForm();
        return p_templatePath;
    }

    private URL getUrl(String p_templatePath)
    {
        return m_classLoader.getResource(resourceName(p_templatePath));
    }

    private String resourceName(String p_templatePath)
    {
        return m_templateSourceDir + p_templatePath + m_extension;
    }

    private URLConnection getTemplate(String p_templatePath)
        throws IOException
    {
        return getUrl(p_templatePath).openConnection();
    }

    private final ClassLoader m_classLoader;
    private final String m_templateSourceDir;
    private final String m_extension;

    public TemplateLocation getTemplateLocation(String p_templatePath)
    {
        return new TemplateResourceLocation(resourceName(p_templatePath));
    }

    public void loadProperties(String p_dirPath, Properties p_properties)
        throws IOException
    {
        InputStream inputStream = m_classLoader.getResourceAsStream(
            m_templateSourceDir + p_dirPath + "/jamon.properties");
        if (inputStream != null)
        {
            p_properties.load(inputStream);
        }
    }
}
