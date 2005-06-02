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

import org.jamon.ParserError;
import org.jamon.emit.EmitMode;
import org.jamon.node.Location;

public abstract class AbstractInnerUnitCallStatement
    extends AbstractCallStatement
{
    AbstractInnerUnitCallStatement(String p_path,
                                   ParamValues p_params,
                                   Unit p_unit,
                                   Location p_location,
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
        return p_fragmentUnitIntf.getFragmentInterfaceName();
    }

    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer,
                               EmitMode p_emitMode) throws ParserError
    {
        generateSourceLine(p_writer);
        p_writer.openBlock();
        makeFragmentImplClasses(m_unit.getFragmentArgsList(),
                                p_writer,
                                p_describer,
                                p_emitMode);
        generateSourceLine(p_writer);
        p_writer.print("__jamon_innerUnit__" + getPath());
        p_writer.openList();
        p_writer.printArg(ArgNames.WRITER);
        //FIXME - do we need to surround args with parens?
        getParams().generateRequiredArgs(m_unit.getSignatureRequiredArgs(),
                                         p_writer);
        for (Iterator<OptionalArgument> o = m_unit.getSignatureOptionalArgs(); 
             o.hasNext(); )
        {
            OptionalArgument arg = o.next();
            String name = arg.getName();
            String expr = getParams().getOptionalArgValue(name);
            p_writer.printArg(expr == null ? getDefault(arg) : expr);
        }
        generateFragmentParams(p_writer,
                               m_unit.getFragmentArgsList().iterator());
        p_writer.closeList();
        p_writer.println(";");
        checkSuppliedParams();
        p_writer.closeBlock();
    }

    protected abstract String getDefault(OptionalArgument p_arg);
}
