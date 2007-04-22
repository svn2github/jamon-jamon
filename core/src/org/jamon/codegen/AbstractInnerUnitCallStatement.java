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

import org.jamon.ParserErrorImpl;

public abstract class AbstractInnerUnitCallStatement
    extends AbstractCallStatement
{
    AbstractInnerUnitCallStatement(String p_path,
                                   ParamValues p_params,
                                   Unit p_unit,
                                   org.jamon.api.Location p_location,
                                   String p_templateIdentifier)
    {
        super(p_path, p_params, p_location, p_templateIdentifier);
        m_unit = p_unit;
    }

    private final Unit m_unit;

    protected Unit getUnit()
    {
        return m_unit;
    }

    @Override
    protected String getFragmentIntfName(FragmentUnit p_fragmentUnitIntf)
    {
        return p_fragmentUnitIntf.getFragmentInterfaceName(true);
    }

    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer) throws ParserErrorImpl
    {
        generateSourceLine(p_writer);
        p_writer.openBlock();
        makeFragmentImplClasses(m_unit.getFragmentArgs(),
                                p_writer,
                                p_describer);
        generateSourceLine(p_writer);
        p_writer.print("__jamon_innerUnit__" + getPath());
        p_writer.openList();
        p_writer.printListElement(ArgNames.WRITER);
        //FIXME - do we need to surround args with parens?
        getParams().generateRequiredArgs(m_unit.getSignatureRequiredArgs(),
                                         p_writer);
        for (OptionalArgument arg: m_unit.getSignatureOptionalArgs())
        {
            String name = arg.getName();
            String expr = getParams().getOptionalArgValue(name);
            p_writer.printListElement(expr == null ? getDefault(arg) : expr);
        }
        generateFragmentParams(p_writer, m_unit.getFragmentArgs());
        p_writer.closeList();
        p_writer.println(";");
        checkSuppliedParams();
        p_writer.closeBlock();
    }

    protected abstract String getDefault(OptionalArgument p_arg);
}
