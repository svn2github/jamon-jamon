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

import org.jamon.JamonRuntimeException;
import org.jamon.ParserError;
import org.jamon.node.Location;
import org.jamon.emit.EmitMode;

public class ComponentCallStatement
    extends AbstractCallStatement
{
    ComponentCallStatement(String p_path,
                           ParamValues p_params,
                           Location p_location,
                           String p_templateIdentifier)
    {
        super(p_path, p_params, p_location, p_templateIdentifier);
    }

    protected String getFragmentIntfName(FragmentUnit p_fragmentUnitIntf)
    {
        return getComponentProxyClassName()
            + "." + p_fragmentUnitIntf.getFragmentInterfaceName();
    }

    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer,
                               EmitMode p_emitMode) throws ParserError
    {
        generateSourceLine(p_writer);
        p_writer.openBlock();
        TemplateDescription desc;
        try
        {
            desc = p_describer.getTemplateDescription(getPath(),
                                                      getLocation(),
                                                      getTemplateIdentifier());
        }
        catch (java.io.IOException e)
        {
            throw new JamonRuntimeException(e);
        }

        makeFragmentImplClasses(desc.getFragmentInterfaces(),
                                p_writer,
                                p_describer,
                                p_emitMode);
        String instanceVar = getUniqueName();
        p_writer.println(getComponentProxyClassName() + " "
                         + instanceVar + " = "
                         + "new " + getComponentProxyClassName()
                         +"(this.getTemplateManager());");

        for (Iterator i = desc.getOptionalArgs().iterator(); i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            String value = getParams().getOptionalArgValue(arg.getName());
            if (value != null)
            {
                p_writer.println(instanceVar + "." + arg.getSetterName()
                                 + "(" + value + ");");
            }
        }
        p_writer.print(instanceVar + ".renderNoFlush");
        p_writer.openList();
        p_writer.printArg(ArgNames.WRITER);
        getParams().generateRequiredArgs(desc.getRequiredArgs().iterator(),
                                         p_writer);
        generateFragmentParams(p_writer,
                               desc.getFragmentInterfaces().iterator());
        p_writer.closeList();
        p_writer.println(";");
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
