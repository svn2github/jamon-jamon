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

import org.jamon.node.AArg;
import org.jamon.node.ADefault;
import org.jamon.util.StringUtils;

public class OptionalArgument extends AbstractArgument
{
    public OptionalArgument(AArg p_arg, ADefault p_default)
    {
        super(p_arg);
        m_default = p_default.getArgexpr().toString().trim();
    }

    public OptionalArgument(String p_name, String p_type, String p_default)
    {
        super(p_name, p_type);
        m_default = p_default;
    }

    public void setDefault(String p_default)
    {
        m_default = p_default;
    }

    public String getDefault()
    {
        return m_default;
    }

    public String getIsNotDefaultName()
    {
        return "get" + StringUtils.capitalize(getName()) + "__IsNotDefault";
    }

    public void generateImplDataCode(IndentingWriter p_writer)
    {
        super.generateImplDataCode(p_writer);

        p_writer.println("public boolean " + getIsNotDefaultName() + "()");
        p_writer.openBlock();
        p_writer.println("return m_" + getName() + "__IsNotDefault;");
        p_writer.closeBlock();

        p_writer.println("private boolean m_" + getName() + "__IsNotDefault;");
    }

    protected void generateImplDataSetterCode(IndentingWriter p_writer)
    {
        super.generateImplDataSetterCode(p_writer);
        p_writer.println("m_" + getName() + "__IsNotDefault = true;");

    }

    private String m_default;
}
