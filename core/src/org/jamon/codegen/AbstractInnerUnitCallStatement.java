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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.jamon.node.Token;

public abstract class AbstractInnerUnitCallStatement
    extends AbstractCallStatement
{
    AbstractInnerUnitCallStatement(String p_path,
                                   Map p_params,
                                   Unit p_unit,
                                   Token p_token,
                                   String p_templateIdentifier)
    {
        super(p_path, p_params, p_token, p_templateIdentifier);
        m_unit = p_unit;
    }

    private final Unit m_unit;

    protected Unit getUnit()
    {
        return m_unit;
    }

    protected String getFragmentIntfName(FragmentUnit p_fragmentUnitIntf)
    {
        return p_fragmentUnitIntf.getFragmentInterfaceName();
    }

    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer)
        throws IOException
    {
        generateSourceLine(p_writer);
        p_writer.openBlock();
        makeFragmentImplClasses(
            m_unit.getFragmentArgsList(), p_writer, p_describer);
        p_writer.print("__jamon_innerUnit__" + getPath());
        p_writer.openList();
        //FIXME - do we need to surround args with parens?
        for (Iterator r = m_unit.getSignatureRequiredArgs(); r.hasNext(); )
        {
            String name = ((RequiredArgument) r.next()).getName();
            String expr = (String) getParams().remove(name);
            if (expr == null)
            {
                throw new AnalysisException(
                    "No value supplied for required argument " + name,
                    getTemplateIdentifier(),
                    getToken());
            }
            p_writer.printArg("(" + expr + ")");
        }
        for (Iterator o = m_unit.getSignatureOptionalArgs(); o.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) o.next();
            String name = arg.getName();
            String expr = (String) getParams().remove(name);
            p_writer.printArg(
                "(" + (expr == null ? getDefault(arg) : expr ) + ")");
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
