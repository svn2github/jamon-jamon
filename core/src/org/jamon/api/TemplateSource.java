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

package org.jamon.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * An interface representing a "repository" of template sources. One
 * obvious implementation would be a file-system based implementation
 * (e.g. {@link org.jamon.compiler.FileTemplateSource}), but others might include storing
 * templates in a database, or retrieving them remotely via HTTP.
 */

public interface TemplateSource
{
    /**
     * Determines when the indicated template was last modified, in ms
     * since the epoch.
     *
     * @param p_templatePath the path to the template
     *
     * @return the timestamp of when the template was last modified
     */
    long lastModified(String p_templatePath)
        throws IOException;

    /**
     * Determines whether the indicated template source is available.
     *
     * @param p_templatePath the path to the template
     *
     * @return whether the template source is available
     */
    boolean available(String p_templatePath)
        throws IOException;

    /**
     * Get a {@link InputStream} for the source of the specified template.
     *
     * @param p_templatePath the path to the template
     *
     * @return an InputStream for the data comprising the template
     */
    InputStream getStreamFor(String p_templatePath)
        throws IOException;

    /**
     * Get an identifying string for the specified template.
     *
     * @param p_templatePath the path to the template
     *
     * @return an identifying string
     */
    String getExternalIdentifier(String p_templatePath);

    TemplateLocation getTemplateLocation(String p_templatePath);

    /**
     * Load any properties that might be used to influence the processing of
     * templates within the specified directory.
     * @param p_path The directory to to look for
     * @param p_properties The {@code Properties} instance to add any found
     *        properties to.
     * @throws IOException
     */
    void loadProperties(String p_path, Properties p_properties)
        throws IOException;
}
