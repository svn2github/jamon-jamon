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
 * The Original Code is Jamon code, released ??.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.modusponens.jtt;

/**
 * A <code>TemplateResolver</code> provides methods to translate
 * template paths to the corresponding interface class name,
 * implementation class name, and the package name for each.
 */

public class TemplateResolver
{
    private static final String FS = System.getProperty("file.separator");

    /**
     * Construct a <code>TemplateResolver</code> specifying the prefix
     * to prepend to the package names for both interface and
     * implementation template classes.
     *
     * @param p_packagePrefix the package prefix
     */
    public TemplateResolver(String p_packagePrefix)
    {
        m_packagePrefix = p_packagePrefix;
    }

    /**
     * Given a template path, return the name of the interface class
     * for that template.
     *
     * @param p_path the template path
     *
     * @return the name of the interface class
     */

    public String getIntfClassName(final String p_path)
    {
        int i = p_path.lastIndexOf(FS);
        return i < 0 ? p_path : p_path.substring(i+1);
    }

    /**
     * Given a template path, return the name of the implementation
     * class for that template.
     *
     * @param p_path the template path
     *
     * @return the name of the implementation class
     */

    public String getImplClassName(final String p_path)
    {
        return getIntfClassName(p_path) + "Impl";
    }


    /**
     * Given a template path, return the name of the package in which
     * the interface class for that template lives.
     *
     * @param p_path the template path
     *
     * @return the name of the interface class package
     */

    public String getIntfPackageName(final String p_path)
    {
        StringBuffer pkg = new StringBuffer();
        if (! "".equals(m_packagePrefix))
        {
            pkg.append(m_packagePrefix);
        }
        int i = p_path.lastIndexOf(FS);
        if (i > 0)
        {
            pkg.append(StringUtils.pathToClassName(p_path.substring(0,i)));
        }
        else
        {
            pkg.deleteCharAt(pkg.length()-1);
        }
        return pkg.toString();
    }


    /**
     * Given a template path, return the name of the package in which
     * the implementation class for that template lives.
     *
     * @param p_path the template path
     *
     * @return the name of the implementation class package
     */

    public String getImplPackageName(final String p_path)
    {
        return getIntfPackageName(p_path);
    }

    /** the package prefix */
    private final String m_packagePrefix;
}
