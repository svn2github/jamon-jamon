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

public class DefCallStatement
    extends AbstractCallStatement
{
    DefCallStatement(String p_path, Map p_params, TemplateUnit p_templateUnit)
    {
        super(p_path, p_params, p_templateUnit);
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
        p_writer.println("__jamon_def__" + getPath() + "(");
        p_writer.indent(2);
        //FIXME - we should have this. make a proto def unit on first
        //pass, fill it in on second pass.  Then no need for getTemplateUnit.
        DefUnit unit = getTemplateUnit().getDefUnit(getPath());
        boolean argsAlreadyPrinted = false;
        for (Iterator r = unit.getRequiredArgs(); r.hasNext(); /* */)
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
                    ("No value supplied for required argument " + name);
            }
            p_writer.print("(" + expr + ")");
        }
        for (Iterator o = unit.getOptionalArgs(); o.hasNext(); /* */ )
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
                p_writer.print(arg.getDefault());
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
        handleFragmentParams(unit.getFragmentArgsList(),
                             p_writer,
                             p_resolver,
                             p_describer,
                             argsAlreadyPrinted);
        p_writer.outdent(2);
        p_writer.println(");");
        checkSuppliedParams();
    }
}
