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
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import java.util.Iterator;

import org.jamon.node.AArg;
import org.jamon.node.AType;
import org.jamon.node.PName;

public abstract class AbstractArgument
{
    public AbstractArgument(String p_name, String p_type)
    {
        m_name = p_name;
        m_type = p_type;
    }

    public AbstractArgument(AArg p_arg)
    {
        this(p_arg.getName().getText(),
             asText((AType) p_arg.getType()));
    }

    public String getName()
    {
        return m_name;
    }

    public String getObfuscatedName()
    {
        return "p__jamon__" + getName();
    }

    public String getType()
    {
        return m_type;
    }

    private static String asText(AType p_type)
    {
        StringBuffer str = new StringBuffer();
        str.append(p_type.getName().toString().trim());
        for (Iterator i = p_type.getBrackets().iterator();
             i.hasNext();
             i.next())
        {
            str.append("[]");
        }
        return str.toString();
    }

    private final String m_name;
    private final String m_type;

}
