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

package org.jamon.codegen;

import org.jamon.node.PName;
import org.jamon.node.PType;
import org.jamon.node.AType;
import org.jamon.node.ASimpleName;
import org.jamon.node.AQualifiedName;
import java.util.Iterator;

public class NodeUtils
{
    private NodeUtils() {}

    public static String asString(PName p_name)
    {
        if (p_name instanceof ASimpleName)
        {
            return ((ASimpleName) p_name).getIdentifier().getText();
        }
        else
        {
            AQualifiedName qname = (AQualifiedName) p_name;
            return qname.getIdentifier().getText()
                + "."
                + asString(qname.getName());
        }
    }

    public static String asString(PType p_type)
    {
        AType type = (AType) p_type;
        StringBuffer str = new StringBuffer();
        str.append(asString(type.getName()));
        for (Iterator i = type.getBrackets().iterator();
             i.hasNext();
             i.next())
        {
            str.append("[]");
        }
        return str.toString();
    }
}
