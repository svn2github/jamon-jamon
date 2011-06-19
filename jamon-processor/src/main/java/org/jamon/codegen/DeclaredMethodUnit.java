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

import java.util.Collection;

import org.jamon.compiler.ParserErrorsImpl;

public class DeclaredMethodUnit
    extends AbstractInnerUnit
    implements MethodUnit
{
    public DeclaredMethodUnit(String p_name,
                              TemplateUnit p_parent,
                              ParserErrorsImpl p_errors,
                              org.jamon.api.Location p_location,
                              boolean p_isAbstract)
    {
        super(p_name, p_parent, p_errors, p_location);
        m_isAbstract = p_isAbstract;
    }

    public DeclaredMethodUnit(
        String p_name, TemplateUnit p_parent, ParserErrorsImpl p_errors, org.jamon.api.Location p_location)
    {
        this(p_name, p_parent, p_errors, p_location, false);
    }

    @Override
    public String getOptionalArgDefaultMethod(OptionalArgument p_arg)
    {
        return "__jamon__get_Method_Opt_" + p_arg.getName() + "_default";
    }

    @Override
    public boolean isAbstract()
    {
        return m_isAbstract;
    }

    @Override public boolean doesIO()
    {
        return m_isAbstract
        || super.doesIO()
        || ((TemplateUnit) getParentUnit()).isParent();
    }

    private final boolean m_isAbstract;

    @Override
    public Collection<OptionalArgument> getOptionalArgsWithDefaults()
    {
        return getSignatureOptionalArgs();
    }

    @Override
    public String getDefaultForArg(OptionalArgument p_arg)
    {
        return p_arg.getDefault();
    }

    @Override
    public boolean isOverride()
    {
        return false;
    }
}
