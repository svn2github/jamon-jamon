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
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.util.StringUtils;

import org.jamon.node.Location;

public abstract class AbstractCallStatement
    extends AbstractStatement
    implements CallStatement
{
    AbstractCallStatement(String p_path,
                          ParamValues p_params,
                          Location p_location,
                          String p_templateIdentifier)
    {
        super(p_location, p_templateIdentifier);
        m_path = p_path;
        m_params = p_params;
    }

    public void addFragmentImpl(FragmentUnit p_unit, ParserErrors p_errors)
    {
        m_fragParams.put(p_unit.getName(), p_unit);
    }

    private final String m_path;
    private final ParamValues m_params;
    private final Map<String, FragmentUnit> m_fragParams =
        new HashMap<String, FragmentUnit>();
    private final static String FRAGMENT_IMPL_PREFIX = "__jamon__instanceOf__";
    private static int s_fragmentImplCounter = 0;
    private final Map<FragmentUnit, String> m_fragmentImplNames =
        new HashMap<FragmentUnit, String>();

    protected abstract String getFragmentIntfName(
        FragmentUnit p_fragmentUnitIntf);

    private String getFragmentImplName(FragmentUnit p_fragmentUnitIntf)
    {
        if(! m_fragmentImplNames.containsKey(p_fragmentUnitIntf))
        {
            m_fragmentImplNames
                .put(p_fragmentUnitIntf,
                     FRAGMENT_IMPL_PREFIX + (s_fragmentImplCounter++) + "__"
                     + p_fragmentUnitIntf.getFragmentInterfaceName(false)
                     );
        }
        return m_fragmentImplNames.get(p_fragmentUnitIntf);
    }

    private void makeFragmentImplClass(FragmentUnit p_fragmentUnitIntf,
                                       CodeWriter p_writer,
                                       TemplateDescriber p_describer) throws ParserError
    {
        final FragmentUnit fragmentUnitImpl =
            m_fragParams.remove(p_fragmentUnitIntf.getName());
        if (fragmentUnitImpl == null)
        {
            throw new ParserError(
                getLocation(),
                "Call is missing fragment " + p_fragmentUnitIntf.getName());
        }

        p_writer.println(
            "class " + getFragmentImplName(p_fragmentUnitIntf));
        p_writer.println("  extends " + ClassNames.BASE_TEMPLATE);
        p_writer.println("  implements " + getFragmentIntfName(p_fragmentUnitIntf));
        p_writer.openBlock();
        p_writer.println(
            "public " + getFragmentImplName(p_fragmentUnitIntf)
            + "(" + ClassNames.TEMPLATE_MANAGER + " p_manager)");
        p_writer.openBlock();
        p_writer.println("super(p_manager);");
        p_writer.closeBlock();
        p_writer.print("public " + ClassNames.RENDERER + " makeRenderer");
        p_writer.openList();
        fragmentUnitImpl.printRenderArgsDecl(p_writer);
        p_writer.closeList();
        p_writer.println();
        p_writer.openBlock();
        p_writer.print(  "return new " + ClassNames.ABSTRACT_RENDERER + "()");
        p_writer.openBlock();
        p_writer.println("@Override");
        p_writer.println(
            "public void renderTo(" + ArgNames.ANNOTATED_WRITER_DECL + ")");
        p_writer.println("  throws " + ClassNames.IOEXCEPTION);
        p_writer.openBlock();
        p_writer.print("renderNoFlush");
        p_writer.openList();
        p_writer.printListElement(ArgNames.WRITER);
        fragmentUnitImpl.printRenderArgs(p_writer);
        p_writer.closeList();
        p_writer.println(";");
        p_writer.closeBlock();
        p_writer.closeBlock(";");
        p_writer.closeBlock();

        p_writer.print("public void renderNoFlush");
        p_writer.openList();
        p_writer.printListElement(ArgNames.ANNOTATED_WRITER_DECL);
        fragmentUnitImpl.printRenderArgsDecl(p_writer);
        p_writer.closeList();
        p_writer.println(" throws "+ ClassNames.IOEXCEPTION);
        fragmentUnitImpl.generateRenderBody(p_writer, p_describer);

        p_writer.closeBlock();
    }

    protected void makeFragmentImplClasses(
        List<FragmentArgument> p_fragmentInterfaces,
        CodeWriter p_writer,
        TemplateDescriber p_describer) throws ParserError
    {
        if (m_fragParams.size() == 1
            && m_fragParams.keySet().iterator().next() == null)
        {
            if(p_fragmentInterfaces.size() == 0)
            {
                throw new ParserError(
                    getLocation(),
                    "Call provides a fragment, but none are expected");
            }
            else if (p_fragmentInterfaces.size() > 1)
            {
                throw new ParserError(getLocation(),
                                      "Call must provide multiple fragments");
            }
            else
            {
                m_fragParams.put(p_fragmentInterfaces.get(0).getName(),
                                 m_fragParams.remove(null));
            }
        }
        for (FragmentArgument arg : p_fragmentInterfaces)
        {
            makeFragmentImplClass
                (arg.getFragmentUnit(),
                 p_writer,
                 p_describer);
        }
    }

    protected void generateFragmentParams(
        CodeWriter p_writer,
        Iterator<FragmentArgument> p_fragmentInterfaces)
    {
        while (p_fragmentInterfaces.hasNext())
        {
            p_writer.printListElement(
                "new "
                + getFragmentImplName(
                    (p_fragmentInterfaces.next().getFragmentUnit()))
                + "(this.getTemplateManager())");
        }
    }

    protected void checkSuppliedParams()
        throws ParserError
    {
        if (getParams().hasUnusedParams())
        {
            throw constructExtraParamsException(
                "arguments", getParams().getUnusedParams());
        }
        if (! m_fragParams.isEmpty())
        {
            throw constructExtraParamsException(
                "fragments", m_fragParams.keySet().iterator());
        }
    }

    ParserError constructExtraParamsException(String p_paramType,
                                              Iterator<String> p_extraParams)
    {
        StringBuilder message = new StringBuilder("Call provides unused ");
        message.append(p_paramType);
        message.append(" ");
        StringUtils.commaJoin(message, p_extraParams);
        return new ParserError(getLocation(), message.toString());
    }

    protected final String getPath()
    {
        return m_path;
    }

    protected final ParamValues getParams()
    {
        return m_params;
    }
}
