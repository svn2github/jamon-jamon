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

public class ComponentCallStatement
    extends AbstractCallStatement
{
    ComponentCallStatement(String p_path,
                           Map p_params,
                           Token p_token,
                           String p_templateIdentifier)
    {
        super(p_path, p_params, p_token, p_templateIdentifier);
    }

    protected String getFragmentIntfName(FragmentUnit p_fragmentUnitIntf)
    {
        return getComponentProxyClassName()
            + "." + p_fragmentUnitIntf.getFragmentInterfaceName();
    }

    public void generateSource(IndentingWriter p_writer,
                               TemplateDescriber p_describer)
        throws IOException
    {
        generateSourceLine(p_writer);
        p_writer.openBlock();
        TemplateDescription desc =
            p_describer.getTemplateDescription(getPath(),
                                               getToken(),
                                               getTemplateIdentifier());

        makeFragmentImplClasses(desc.getFragmentInterfaces(),
                                p_writer,
                                p_describer);
        String instanceVar = getUniqueName();
        p_writer.println(getComponentProxyClassName() + " "
                         + instanceVar + " = "
                         + "new " + getComponentProxyClassName()
                         +"(this.getTemplateManager())");
        p_writer.indent(2);
        p_writer.println(".escapeWith(this.getEscaping());");
        p_writer.outdent(2);

        for (Iterator i = desc.getOptionalArgs().iterator(); i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            String value = (String) getParams().remove(arg.getName());
            if (value != null)
            {
                p_writer.println(instanceVar + "." + arg.getSetterName()
                                 + "(" + value + ");");
            }
        }
        p_writer.print(instanceVar + ".render(this.getWriter()");
        for (Iterator i = desc.getRequiredArgs().iterator(); i.hasNext(); )
        {
            p_writer.println(", ");
            String name = ((RequiredArgument) i.next()).getName();
            String expr = (String) getParams().remove(name);
            if (expr == null)
            {
                throw new AnalysisException(
                    "No value supplied for required argument " + name,
                    getTemplateIdentifier(),
                    getToken());
            }
            p_writer.print(expr);
        }
        generateFragmentParams(p_writer,
                               desc.getFragmentInterfaces().iterator(),
                               true);
        p_writer.println(");");
        checkSuppliedParams();
        p_writer.closeBlock();
    }

    private String getComponentProxyClassName()
    {
        return PathUtils.getFullyQualifiedIntfClassName(getPath());
    }

    private static String getUniqueName()
    {
        return "__jamon__var_" + m_uniqueId++;
    }

    private static int m_uniqueId = 0;
}
