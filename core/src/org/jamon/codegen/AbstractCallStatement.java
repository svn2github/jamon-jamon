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

import org.jamon.JamonException;
import org.jamon.util.StringUtils;

public abstract class AbstractCallStatement
    implements CallStatement
{
    AbstractCallStatement(String p_path, Map p_params)
    {
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

    protected abstract String getFragmentIntfName(
        FragmentUnit p_fragmentUnitIntf, TemplateResolver p_resolver);

    private void handleFragmentParam(FragmentUnit p_fragmentUnitIntf,
                                     IndentingWriter p_writer,
                                     TemplateResolver p_resolver,
                                     TemplateDescriber p_describer)
        throws IOException
    {
        final FragmentUnit fragmentUnitImpl =
            (FragmentUnit) m_fragParams.remove(p_fragmentUnitIntf.getName());
        if (fragmentUnitImpl == null)
        {
            throw new JamonException
                ("Call to " + getPath()
                 + " is missing fragment " + p_fragmentUnitIntf.getName());
        }

        p_writer.print("new "
                       + getFragmentIntfName(p_fragmentUnitIntf, p_resolver)
                       + "(this.getTemplateManager(), this.getEscaping()) ");
        p_writer.openBlock();

        p_writer.print("public " + ClassNames.RENDERER + " makeRenderer(");
        fragmentUnitImpl.printRequiredArgsDecl(p_writer);
        p_writer.println(")");
        p_writer.openBlock();
        p_writer.print(  "return new " + ClassNames.RENDERER + "()");
        p_writer.openBlock();
        p_writer.print(  "public void renderTo(");
        p_writer.print(  ClassNames.WRITER);
        p_writer.println(" p_writer)");
        p_writer.print(  "  throws ");
        p_writer.println(ClassNames.IOEXCEPTION);
        p_writer.openBlock();
        p_writer.println("writeTo(p_writer);");
        p_writer.print  ("render(");
        fragmentUnitImpl.printRequiredArgs(p_writer);
        p_writer.println(");");
        p_writer.closeBlock();
        p_writer.closeBlock(";");
        p_writer.closeBlock();

        p_writer.print("public void render(");
        fragmentUnitImpl.printRequiredArgsDecl(p_writer);
        p_writer.print(") throws ");
        p_writer.println(ClassNames.IOEXCEPTION);
        fragmentUnitImpl.generateRenderBody(p_writer, p_resolver, p_describer);

        p_writer.closeBlock();
    }

    protected void handleFragmentParams(List p_fragmentInterfaces,
                                      IndentingWriter p_writer,
                                      TemplateResolver p_resolver,
                                      TemplateDescriber p_describer,
                                      boolean p_argsAlreadyPrinted)
        throws IOException
    {
        if (m_fragParams.size() == 1
            && m_fragParams.keySet().iterator().next() == null)
        {
            if(p_fragmentInterfaces.size() == 0)
            {
                throw new JamonException
                    ("Call to " + getPath()
                     + " provides a fragment, but none are expected");
            }
            else if (p_fragmentInterfaces.size() > 1)
            {
                throw new JamonException
                    ("Call to " + getPath()
                     + " must provide multiple fragments");
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
            if (p_argsAlreadyPrinted)
            {
                p_writer.println(", ");
            }
            p_argsAlreadyPrinted = true;
            handleFragmentParam
                (((FragmentArgument) i.next()).getFragmentUnit(),
                 p_writer,
                 p_resolver,
                 p_describer);
        }
    }

    protected void checkSuppliedParams()
        throws JamonException
    {
        checkSuppliedParams("arguments", m_params);
        checkSuppliedParams("fragments", m_fragParams);
    }

    private void checkSuppliedParams(String p_paramType, Map p_params)
        throws JamonException
    {
        if (! p_params.isEmpty())
        {
            StringBuffer message = new StringBuffer("Call to ");
            message.append(getPath());
            message.append(" provides unused ");
            message.append(p_paramType);
            message.append(" ");
            StringUtils.commaJoin(message, p_params.keySet().iterator());
            throw new JamonException(message.toString());
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
