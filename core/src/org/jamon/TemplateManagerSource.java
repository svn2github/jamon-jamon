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

/**
   The source for obtaining a default TemplateManager.  Note that this
   is an abstract class instead of an interface in order to allow
   static methods.
 */

public abstract class TemplateManagerSource
{
    /**
     * Get a {@link TemplateManager} for a specified template path.
     *
     * @param p_path the template path
     *
     * @return a TemplateManager appropriate for that path
     */
    public abstract TemplateManager getTemplateManagerForPath(String p_path);

    public static TemplateManager getTemplateManagerFor(String p_path)
    {
        return getTemplateManagerSource().getTemplateManagerForPath(p_path);
    }

    public static void setTemplateManagerSource(TemplateManagerSource p_source)
    {
        s_source = p_source;
    }

    public static void setTemplateManager(final TemplateManager p_manager)
    {
        setTemplateManagerSource(new TemplateManagerSource()
            {
                public TemplateManager getTemplateManagerForPath(String p_path)
                {
                    return p_manager;
                }
            });
    }

    private static TemplateManagerSource getTemplateManagerSource()
    {
        return s_source;
    }

    private static TemplateManagerSource s_source;

    static
    {
        setTemplateManager(new BasicTemplateManager());
    }
}
