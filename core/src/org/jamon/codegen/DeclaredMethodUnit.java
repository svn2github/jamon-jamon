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

public class DeclaredMethodUnit
    extends AbstractInnerUnit
    implements MethodUnit
{
    public DeclaredMethodUnit(String p_name,
                              Unit p_parent,
                              boolean p_isAbstract)
    {
        super(p_name, p_parent);
        m_isAbstract = p_isAbstract;
    }

    public DeclaredMethodUnit(String p_name, Unit p_parent)
    {
        this(p_name, p_parent, false);
    }

    public String getOptionalArgDefaultMethod(OptionalArgument p_arg)
    {
        return "__jamon__get_Method_Opt_" + p_arg.getName() + "_default";
    }

    public boolean isAbstract()
    {
        return m_isAbstract;
    }

    private final boolean m_isAbstract;
}
