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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.HashMap;

import org.jamon.JamonException;
import org.jamon.util.StringUtils;

public class CallStatement
    implements Statement
{
    CallStatement(String p_path, Map p_params, TemplateUnit p_templateUnit)
    {
        m_path = p_path;
        m_params = p_params;
        m_templateUnit = p_templateUnit;
    }

    public void addFragmentImpl(FragmentUnit p_unit)
    {
        m_fragParams.put(p_unit.getName(), p_unit);
    }

    private final String m_path;
    private final Map m_params;
    private final TemplateUnit m_templateUnit;
    private final Map m_fragParams = new HashMap();;

    private boolean isDefCall()
    {
        return m_templateUnit.doesDefExist(getPath());
    }

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

        String fragmentIntf = p_fragmentUnitIntf.getFragmentInterfaceName();
        if (! isDefCall())
        {
            fragmentIntf =
                p_resolver.getFullyQualifiedIntfClassName
                    (m_templateUnit.getAbsolutePath(getPath()))
                + "." + fragmentIntf;
        }

        p_writer.print("new " + fragmentIntf
                       + "(this.getTemplateManager(), \"\") ");
        p_writer.openBlock();

        p_writer.print("public " + ClassNames.RENDERER + " makeRenderer(");
        p_fragmentUnitIntf.printRequiredArgsDecl(p_writer);
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
        p_fragmentUnitIntf.printRequiredArgs(p_writer);
        p_writer.println(");");
        p_writer.closeBlock();
        p_writer.closeBlock(";");
        p_writer.closeBlock();

        p_writer.print("public void render(");
        fragmentUnitImpl.printRequiredArgsDecl(p_writer);
        p_writer.print(") throws ");
        p_writer.println(ClassNames.IOEXCEPTION);
        p_writer.openBlock();
        fragmentUnitImpl.printArgDeobfuscations(p_writer);
        for (Iterator i = fragmentUnitImpl.getStatements().iterator();
             i.hasNext(); )
        {
            ((Statement)i.next()).generateSource(p_writer,
                                                 p_resolver,
                                                 p_describer);
            p_writer.println();
        }
        p_writer.closeBlock();

        p_writer.closeBlock();
    }

    private void handleFragmentParams(List p_fragmentInterfaces,
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

    public void generateSource(IndentingWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer)
        throws IOException
    {
        if (isDefCall())
        {
            generateAsDefCall(p_writer, p_resolver, p_describer);
        }
        else
        {
            generateAsComponentCall(p_writer,
                                    p_resolver,
                                    p_describer,
                                    m_templateUnit.getAbsolutePath(m_path));
        }
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

    private void generateAsDefCall(IndentingWriter p_writer,
                                   TemplateResolver p_resolver,
                                   TemplateDescriber p_describer)
        throws IOException
    {
        p_writer.println("__jamon_def__" + getPath() + "(");
        p_writer.indent(2);
        DefUnit unit = m_templateUnit.getDefUnit(getPath());
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
            String expr = (String) m_params.remove(name);
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
            String expr = (String) m_params.remove(name);
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
    }

    private void generateAsComponentCall(IndentingWriter p_writer,
                                         TemplateResolver p_resolver,
                                         TemplateDescriber p_describer,
                                         String p_absPath)
        throws IOException
    {
        String intfName = p_resolver.getFullyQualifiedIntfClassName(p_absPath);
        p_writer.print("new ");
        p_writer.print(intfName);
        p_writer.println("(this.getTemplateManager())");
        p_writer.indent(5);
        p_writer.println(".writeTo(this.getWriter())");
        p_writer.println(".escaping(this.getEscaping())");

        TemplateDescription desc =
            p_describer.getTemplateDescription(p_absPath);

        for (Iterator i = desc.getOptionalArgs().iterator(); i.hasNext(); )
        {
            String name = ((OptionalArgument) i.next()).getName();
            String value = (String) m_params.remove(name);
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
            String expr = (String) m_params.remove(name);
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
    }

    private final String getPath()
    {
        return m_path;
    }
}
