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
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.util.Iterator;

import org.jamon.node.AAliasPath;
import org.jamon.node.AAliasedPath;
import org.jamon.node.ASimpleName;
import org.jamon.node.ARelPath;
import org.jamon.node.ARelativePath;
import org.jamon.node.AQualifiedName;
import org.jamon.node.AAbsPath;
import org.jamon.node.AAbsolutePath;
import org.jamon.node.AType;
import org.jamon.node.PName;
import org.jamon.node.PPath;


public class NodeUtils
{
    private NodeUtils() {}

    public static Path asText(PPath node)
    {
        if (node instanceof ARelPath)
        {
            return new Path(null,
                            asText((ARelativePath) ((ARelPath) node).getRelativePath()));
        }
        else if (node instanceof AAbsPath)
        {
            return new Path(null,
                            asText((AAbsolutePath) ((AAbsPath)node).getAbsolutePath()));
        }
        else
        {
            AAliasedPath path =
                (AAliasedPath) ((AAliasPath)node).getAliasedPath();
            return new Path(path.getIdentifier() == null
                            ? ""
                            : path.getIdentifier().getText(),
                            asText((AAbsolutePath) path.getAbsolutePath()));
        }
    }

    private static String asText(AAbsolutePath p_path)
    {
        if (p_path == null)
        {
            return "";
        }
        else
        {
            return "/" + asText((ARelativePath) p_path.getRelativePath());
        }
    }

    private static String asText(ARelativePath p_path)
    {
        return p_path.getIdentifier().getText()
            + asText((AAbsolutePath) p_path.getAbsolutePath());
    }

    public static String asText(PName name)
    {
        if (name instanceof ASimpleName)
        {
            return ((ASimpleName)name).getIdentifier().getText();

        }
        else if (name instanceof AQualifiedName)
        {
            AQualifiedName qname = (AQualifiedName) name;
            return asText(qname.getName())
                + '.'
                + qname.getIdentifier().getText();
        }
        else
        {
            throw new RuntimeException("Unknown name type "
                                       + name.getClass().getName());
        }
    }

    public static String asText(AType p_type)
    {
        StringBuffer str = new StringBuffer();
        str.append(asText(p_type.getName()));
        for (Iterator i = p_type.getBrackets().iterator();
             i.hasNext();
             i.next())
        {
            str.append("[]");
        }
        return str.toString();
    }
}
