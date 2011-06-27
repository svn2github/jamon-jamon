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

import java.util.List;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.node.GenericCallParam;

public class ComponentCallStatement
    extends AbstractCallStatement
{
    ComponentCallStatement(String p_path,
                           ParamValues p_params,
                           org.jamon.api.Location p_location,
                           String p_templateIdentifier,
                           List<GenericCallParam> p_genericParams,
                           String p_callingTemplateJamonContextType)
    {
        super(p_path, p_params, p_location, p_templateIdentifier);
        m_genericParams = p_genericParams;
        m_callingTemplateJamonContextType = p_callingTemplateJamonContextType;
    }

    @Override
    protected String getFragmentIntfName(FragmentUnit p_fragmentUnitIntf)
    {
        return getComponentProxyClassName()
            + "." + p_fragmentUnitIntf.getFragmentInterfaceName(false)
            + getGenericParams();
    }

    @Override
    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer) throws ParserErrorImpl
    {
        generateSourceLine(p_writer);
        p_writer.openBlock();
        TemplateDescription desc;
        try
        {
            desc = p_describer.getTemplateDescription(getPath(),
                                                      getLocation());
        }
        catch (java.io.IOException e)
        {
            throw new RuntimeException(e);
        }

        if (desc.getJamonContextType() != null
            && m_callingTemplateJamonContextType == null)
        {
            throw new ParserErrorImpl(
                getLocation(),
                "Calling component does not have a jamonContext, but called " +
                "component " + getPath() + " expects one of type " +
                desc.getJamonContextType());
        }
        if (hasGenericParams())
        {
            if (desc.getGenericParamsCount() != getGenericParamCount())
            {
                throw new ParserErrorImpl(
                    getLocation(),
                    "Call to component " + getPath() + " provides "
                    + getGenericParamCount() + " generic params, but "
                    + getPath() + " only expects "
                    + desc.getGenericParamsCount());
            }
        }

        makeFragmentImplClasses(desc.getFragmentInterfaces(),
                                p_writer,
                                p_describer);
        String instanceVar = getUniqueName();
        p_writer.println(
            getComponentProxyClassName() + getGenericParams() + " "
            + instanceVar + " = "
            + "new " + getComponentProxyClassName() + getGenericParams()
            +"(this.getTemplateManager());");

        if (desc.getJamonContextType() != null)
        {
            p_writer.println(instanceVar + ".setJamonContext(jamonContext);");
        }

        for (OptionalArgument arg: desc.getOptionalArgs())
        {
            String value = getParams().getOptionalArgValue(arg.getName());
            if (value != null)
            {
                p_writer.println(instanceVar + "." + arg.getSetterName()
                                 + "(" + value + ");");
            }
        }
        p_writer.print(instanceVar + ".renderNoFlush");
        p_writer.openList();
        p_writer.printListElement(ArgNames.WRITER);
        getParams().generateRequiredArgs(desc.getRequiredArgs(), p_writer);
        generateFragmentParams(p_writer, desc.getFragmentInterfaces());
        p_writer.closeList();
        p_writer.println(";");
        checkSuppliedParams();
        p_writer.closeBlock();
    }

    protected boolean hasGenericParams()
    {
        return !m_genericParams.isEmpty();
    }

    protected int getGenericParamCount()
    {
        return m_genericParams.size();
    }

    protected String getGenericParams()
    {
        if (hasGenericParams())
        {
            StringBuilder builder = new StringBuilder();
            builder.append('<');
            boolean paramsAdded = false;
            for (GenericCallParam param : m_genericParams)
            {
                if (paramsAdded)
                {
                    builder.append(", ");
                }
                builder.append(param.getClassName());
                paramsAdded = true;
            }
            builder.append('>');
            return builder.toString();
        }
        else
        {
            return "";
        }
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
    private final List<GenericCallParam> m_genericParams;
    private final String m_callingTemplateJamonContextType;
}
