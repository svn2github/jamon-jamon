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
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.jamon.node.Token;
import org.jamon.util.StringUtils;

public abstract class AbstractCallStatement
    extends AbstractStatement
    implements CallStatement
{
    AbstractCallStatement(String p_path,
                          Map p_params,
                          Token p_token,
                          String p_templateIdentifier)
    {
        super(p_token, p_templateIdentifier);
        m_path = p_path;
        m_params = p_params;
    }

    public void addFragmentImpl(FragmentUnit p_unit)
    {
        m_fragParams.put(p_unit.getName(), p_unit);
    }

    private final String m_path;
    private final Map m_params;
    private final Map m_fragParams = new HashMap();
    private final static String FRAGMENT_IMPL_PREFIX = "__jamon__instanceOf__";
    private static int s_fragmentImplCounter = 0;
    private final Map m_fragmentImplNames = new HashMap();

    protected abstract String getFragmentIntfName(
        FragmentUnit p_fragmentUnitIntf);

    protected void generateRequiredArgs(Iterator p_args, CodeWriter p_writer)
        throws IOException
    {
        while (p_args.hasNext())
        {
            String name = ((RequiredArgument) p_args.next()).getName();
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
    }


    private String getFragmentImplName(FragmentUnit p_fragmentUnitIntf)
    {
        if(! m_fragmentImplNames.containsKey(p_fragmentUnitIntf))
        {
            m_fragmentImplNames
                .put(p_fragmentUnitIntf,
                     FRAGMENT_IMPL_PREFIX + (s_fragmentImplCounter++) + "__"
                     + p_fragmentUnitIntf.getFragmentInterfaceName());
        }
        return (String) m_fragmentImplNames.get(p_fragmentUnitIntf);
    }

    private void makeFragmentImplClass(FragmentUnit p_fragmentUnitIntf,
                                       CodeWriter p_writer,
                                       TemplateDescriber p_describer)
        throws IOException
    {
        final FragmentUnit fragmentUnitImpl =
            (FragmentUnit) m_fragParams.remove(p_fragmentUnitIntf.getName());
        if (fragmentUnitImpl == null)
        {
            throw new AnalysisException
                ("Call is missing fragment " + p_fragmentUnitIntf.getName(),
                 getTemplateIdentifier(),
                 getToken());
        }

        p_writer.println("class " + getFragmentImplName(p_fragmentUnitIntf));
        p_writer.println("  extends " + ClassNames.BASE_TEMPLATE);
        p_writer.println("  implements "
                         + getFragmentIntfName(p_fragmentUnitIntf));
        p_writer.openBlock();
        p_writer.println("public " + getFragmentImplName(p_fragmentUnitIntf)
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
        p_writer.print(  "return new " + ClassNames.RENDERER + "()");
        p_writer.openBlock();
        p_writer.println("public void renderTo(" + ArgNames.WRITER_DECL + ")");
        p_writer.println("  throws " + ClassNames.IOEXCEPTION);
        p_writer.openBlock();
        p_writer.print("renderNoFlush");
        p_writer.openList();
        p_writer.printArg(ArgNames.WRITER);
        fragmentUnitImpl.printRenderArgs(p_writer);
        p_writer.closeList();
        p_writer.println(";");
        p_writer.closeBlock();
        p_writer.closeBlock(";");
        p_writer.closeBlock();

        p_writer.print("public void renderNoFlush");
        p_writer.openList();
        p_writer.printArg(ArgNames.WRITER_DECL);
        fragmentUnitImpl.printRenderArgsDecl(p_writer);
        p_writer.closeList();
        p_writer.println(" throws "+ ClassNames.IOEXCEPTION);
        fragmentUnitImpl.generateRenderBody(p_writer, p_describer);

        p_writer.closeBlock();
    }

    protected void makeFragmentImplClasses(List p_fragmentInterfaces,
                                           CodeWriter p_writer,
                                           TemplateDescriber p_describer)
        throws IOException
    {
        if (m_fragParams.size() == 1
            && m_fragParams.keySet().iterator().next() == null)
        {
            if(p_fragmentInterfaces.size() == 0)
            {
                throw new AnalysisException
                    ("Call provides a fragment, but none are expected",
                     getTemplateIdentifier(),
                     getToken());
            }
            else if (p_fragmentInterfaces.size() > 1)
            {
                throw new AnalysisException
                    ("Call must provide multiple fragments",
                     getTemplateIdentifier(),
                     getToken());
            }
            else
            {
                m_fragParams.put
                    (((AbstractArgument) p_fragmentInterfaces.get(0))
                     .getName(),
                     m_fragParams.remove(null));
            }
        }
        for (Iterator i = p_fragmentInterfaces.iterator(); i.hasNext(); )
        {
            makeFragmentImplClass
                (((FragmentArgument) i.next()).getFragmentUnit(),
                 p_writer,
                 p_describer);
        }
    }

    protected void generateFragmentParams(CodeWriter p_writer,
                                          Iterator p_fragmentInterfaces)
    {
        while (p_fragmentInterfaces.hasNext())
        {
            p_writer.printArg(
                "new "
                + getFragmentImplName(
                    (((FragmentArgument) p_fragmentInterfaces.next())
                     .getFragmentUnit()))
                + "(this.getTemplateManager())");
        }
    }

    protected void checkSuppliedParams()
        throws AnalysisException
    {
        checkSuppliedParams("arguments", m_params);
        checkSuppliedParams("fragments", m_fragParams);
    }

    private void checkSuppliedParams(String p_paramType, Map p_params)
        throws AnalysisException
    {
        if (! p_params.isEmpty())
        {
            StringBuffer message = new StringBuffer("Call provides unused ");
            message.append(p_paramType);
            message.append(" ");
            StringUtils.commaJoin(message, p_params.keySet().iterator());
            throw new AnalysisException(message.toString(),
                                        getTemplateIdentifier(),
                                        getToken());
        }
    }

    protected final String getPath()
    {
        return m_path;
    }

    protected final Map getParams()
    {
        return m_params;
    }
}
