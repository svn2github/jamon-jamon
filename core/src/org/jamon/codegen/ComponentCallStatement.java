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
import org.jamon.util.StringUtils;

public class ComponentCallStatement
    extends AbstractCallStatement
{
    ComponentCallStatement(String p_path, Map p_params)
    {
        super(p_path, p_params);
    }

    protected String getFragmentIntfName(FragmentUnit p_fragmentUnitIntf,
                                       TemplateResolver p_resolver)
    {
        return p_resolver.getFullyQualifiedIntfClassName(getPath())
            + "." + p_fragmentUnitIntf.getFragmentInterfaceName();
    }

    public void generateSource(IndentingWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer)
        throws IOException
    {
        String componentPath = getPath();
        p_writer.println("new "
                         + p_resolver.getFullyQualifiedIntfClassName
                             (componentPath)
                         +"(this.getTemplateManager())");
        p_writer.indent(5);
        p_writer.println(".writeTo(this.getWriter())");
        p_writer.println(".escaping(this.getEscaping())");

        TemplateDescription desc =
            p_describer.getTemplateDescription(componentPath);

        for (Iterator i = desc.getOptionalArgs().iterator(); i.hasNext(); )
        {
            String name = ((OptionalArgument) i.next()).getName();
            String value = (String) getParams().remove(name);
            if (value != null)
            {
                p_writer.print(".set");
                p_writer.print(StringUtils.capitalize(name));
                p_writer.print("(");
                p_writer.print(value);
                p_writer.println(")");
                i.remove();
            }
        }
        p_writer.print(".render(");
        boolean argsAlreadyPrinted = false;
        for (Iterator i = desc.getRequiredArgs().iterator(); i.hasNext(); )
        {
            if (argsAlreadyPrinted)
            {
                p_writer.println(", ");
            }
            String name = ((RequiredArgument) i.next()).getName();
            String expr = (String) getParams().remove(name);
            if (expr == null)
            {
                throw new JamonException("No value supplied for required argument "
                                         + name
                                         + " in call to "
                                         + getPath());
            }
            p_writer.print(expr);
            argsAlreadyPrinted = true;
        }
        handleFragmentParams(desc.getFragmentInterfaces(),
                             p_writer,
                             p_resolver,
                             p_describer,
                             argsAlreadyPrinted);
        p_writer.println(");");
        p_writer.outdent(5);
        checkSuppliedParams();
    }
}
