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
import org.jamon.util.StringUtils;

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
             NodeUtils.asString((AType) p_arg.getType()));
    }

    public String getName()
    {
        return m_name;
    }

    public String getType()
    {
        return m_type;
    }

    public String getSetterName()
    {
        return "set" + StringUtils.capitalize(getName());
    }

    public String getGetterName()
    {
        return "get" + StringUtils.capitalize(getName());
    }

    public void generateImplDataCode(IndentingWriter p_writer)
    {
        p_writer.println( "public void " + getSetterName()
                          + "(" + getType() + " " + getName() + ")");
        p_writer.openBlock();
        generateImplDataSetterCode(p_writer);
        p_writer.closeBlock();

        p_writer.println("public " + getType() + " " + getGetterName() + "()");
        p_writer.openBlock();
        p_writer.println("return m_" + getName() + ";");
        p_writer.closeBlock();
        p_writer.println("private " + getType() + " m_" + getName() + ";");
    }

    protected void generateImplDataSetterCode(IndentingWriter p_writer)
    {
        p_writer.println("m_" + getName() + " = " + getName() + ";");
    }

    private final String m_name;
    private final String m_type;

}
