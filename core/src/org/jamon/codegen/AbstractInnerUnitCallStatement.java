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

import org.jamon.JamonException;

public abstract class AbstractInnerUnitCallStatement
    extends AbstractCallStatement
{
    AbstractInnerUnitCallStatement(String p_path,
                                   Map p_params,
                                   Unit p_unit)
    {
        super(p_path, p_params);
        m_unit = p_unit;
    }

    private final Unit m_unit;
    
    protected Unit getUnit()
    {
        return m_unit;
    }

    protected String getFragmentIntfName(FragmentUnit p_fragmentUnitIntf,
                                         TemplateResolver p_resolver)
    {
        return p_fragmentUnitIntf.getFragmentInterfaceName();
    }

    public void generateSource(IndentingWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer)
        throws IOException
    {
        p_writer.println("__jamon_innerUnit__" + getPath() + "(");
        p_writer.indent(2);
        boolean argsAlreadyPrinted = false;
        for (Iterator r = m_unit.getSignatureRequiredArgs(); r.hasNext(); )
        {
            if (argsAlreadyPrinted)
            {
                p_writer.println(",");
            }
            argsAlreadyPrinted = true;
            RequiredArgument arg = (RequiredArgument) r.next();
            String name = arg.getName();
            String expr = (String) getParams().remove(name);
            if (expr == null)
            {
                throw new JamonException
                    ("No value supplied for required argument " + name
                     + " in call to " + getPath());
            }
            p_writer.print("(" + expr + ")");
        }
        for (Iterator o = m_unit.getSignatureOptionalArgs(); o.hasNext(); )
        {
            if (argsAlreadyPrinted)
            {
                p_writer.println(",");
            }
            argsAlreadyPrinted = true;
            OptionalArgument arg = (OptionalArgument) o.next();
            String name = arg.getName();
            p_writer.print("(");
            String expr = (String) getParams().remove(name);
            if (expr == null)
            {
                printDefault(p_writer, arg);
            }
            else
            {
                p_writer.print(expr);
            }
            p_writer.print(")");
            if (o.hasNext())
            {
                p_writer.println(",");
            }
        }
        handleFragmentParams(m_unit.getFragmentArgsList(),
                             p_writer,
                             p_resolver,
                             p_describer,
                             argsAlreadyPrinted);
        p_writer.outdent(2);
        p_writer.println(");");
        checkSuppliedParams();
    }

    protected abstract void printDefault(IndentingWriter p_writer,
                                         OptionalArgument p_arg)
        throws IOException;
}
