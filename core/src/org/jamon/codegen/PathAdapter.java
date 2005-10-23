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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */
package org.jamon.codegen;

import java.util.Map;

import org.jamon.ParserErrors;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.DepthFirstAnalysisAdapter;
import org.jamon.node.NamedAliasPathNode;
import org.jamon.node.PathElementNode;
import org.jamon.node.RootAliasPathNode;
import org.jamon.node.UpdirNode;


class PathAdapter extends DepthFirstAnalysisAdapter
{
    public PathAdapter(String p_templateDir,
                       final Map<String, String> p_aliases,
                       ParserErrors p_errors)
    {
        m_templateDir = p_templateDir;
        m_aliases = p_aliases;
        m_errors = p_errors;
    }

    private final String m_templateDir;
    private final Map<String, String> m_aliases;
    private final ParserErrors m_errors;
    private final StringBuilder m_path = new StringBuilder();
    private boolean m_absolutePath = false;

    public String getPath()
    {
        return m_path.substring(0, m_path.length() - 1);
    }


    @Override public void inAbsolutePathNode(AbsolutePathNode p_node)
    {
        m_absolutePath = true;
        m_path.append('/');
    }

    @Override public void inUpdirNode(UpdirNode p_updir)
    {
        if (! m_absolutePath)
        {
            m_path.insert(0, m_templateDir);
            m_absolutePath = true;
        }
        int lastSlash = m_path.toString().lastIndexOf('/', m_path.length() - 2 );
        if (lastSlash < 0)
        {
            m_errors.addError(
                "Cannot reference templates above the root",
                p_updir.getLocation());
        }
        m_path.delete(lastSlash + 1, m_path.length());
    }


    @Override public void inPathElementNode(PathElementNode p_relativePath)
    {
        m_path.append(p_relativePath.getName());
        m_path.append('/');
    }

    @Override public void inNamedAliasPathNode(NamedAliasPathNode p_node)
    {
        String alias = m_aliases.get(p_node.getAlias());
        if (alias == null)
        {
            m_errors.addError(
               "Unknown alias " + p_node.getAlias(), p_node.getLocation());
        }
        else
        {
            m_path.append(alias);
            m_path.append('/');
        }
    }

    @Override public void inRootAliasPathNode(RootAliasPathNode p_node)
    {
        String alias = m_aliases.get("/");
        if (alias == null)
        {
            m_errors.addError("Unknown alias " + alias, p_node.getLocation());
        }
        else
        {
            m_path.append(alias);
            m_path.append('/');
        }
    }
}