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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public abstract class AbstractUnit
    implements Unit
{
    public AbstractUnit(String p_name, Unit p_parent)
    {
        m_name = p_name;
        m_parent = p_parent;
    }

    public final String getName()
    {
        return m_name;
    }

    public final Unit getParent()
    {
        return m_parent;
    }

    public abstract void addFragmentArg(FragmentArgument p_arg);
    public abstract Iterator getFragmentArgs();
    public abstract List getFragmentArgsList();

    public FragmentUnit getFragmentUnitIntf(String p_path)
    {
        for (Iterator i = getFragmentArgs(); i.hasNext(); )
        {
            FragmentArgument arg = (FragmentArgument) i.next();
            if (p_path.equals(arg.getName()))
            {
                return arg.getFragmentUnit();
            }
        }
        return null;
    }

    public void addStatement(Statement p_statement)
    {
        m_statements.add(p_statement);
    }

    public List getStatements()
    {
        return m_statements;
    }

    public void generateRenderBody(IndentingWriter p_writer,
                                   TemplateResolver p_resolver,
                                   TemplateDescriber p_describer)
        throws IOException
    {
        p_writer.openBlock();
        printArgDeobfuscations(p_writer);
        printStatements(p_writer, p_resolver, p_describer);
        printRenderBodyEnd(p_writer);
        p_writer.closeBlock();
    }

    private void printArgDeobfuscations(IndentingWriter p_writer)
    {
        for (Iterator i = getVisibleArgs(); i.hasNext(); )
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            p_writer.println("final " + arg.getType() + " " + arg.getName()
                             + " = " + arg.getObfuscatedName() + ";");
        }
    }

    private void printStatements(IndentingWriter p_writer,
                                TemplateResolver p_resolver,
                                TemplateDescriber p_describer)
        throws IOException
    {
        for (Iterator i = getStatements().iterator(); i.hasNext(); )
        {
            ((Statement)i.next()).generateSource(p_writer,
                                                 p_resolver,
                                                 p_describer);
        }
    }

    protected void printRenderBodyEnd(IndentingWriter p_writer)
    {
    }

    public abstract void addRequiredArg(RequiredArgument p_arg);
    public abstract void addOptionalArg(OptionalArgument p_arg);
    public abstract Iterator getSignatureRequiredArgs();
    public abstract Iterator getSignatureOptionalArgs();
    public abstract Iterator getVisibleArgs();

    private final String m_name;
    private final Unit m_parent;
    private final List m_statements = new LinkedList();
    private final Set m_argNames = new HashSet();

    protected void checkArgName(AbstractArgument p_arg)
    {
        if (! m_argNames.add(p_arg.getName()))
        {
            throw new TunnelingException
                (getName() + " has multiple arguments named "
                 + p_arg.getName());
        }
    }



    public void printRequiredArgsDecl(IndentingWriter p_writer)
    {
        printArgsDecl(p_writer,
                      new SequentialIterator(getSignatureRequiredArgs(),
                                             getFragmentArgs()));
    }

    public void printRequiredArgs(IndentingWriter p_writer)
    {
        printArgs(p_writer, new SequentialIterator(getSignatureRequiredArgs(),
                                                   getFragmentArgs()));
    }

    protected static void printArgsDecl(IndentingWriter p_writer,
                                        Iterator i)
    {
        while(i.hasNext())
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            p_writer.print("final " + arg.getType() + " "
                           + arg.getObfuscatedName());
            if (i.hasNext())
            {
                p_writer.print(", ");
            }
        }
    }

    protected static void printArgs(IndentingWriter p_writer,
                                    Iterator p_args)
    {
        while (p_args.hasNext())
        {
            p_writer.print(((AbstractArgument) p_args.next())
                           .getObfuscatedName());
            if(p_args.hasNext())
            {
                p_writer.print(", ");
            }
        }
    }

    protected void generateInterfaceSummary(StringBuffer p_buf)
    {
        p_buf.append("Required\n");
        for (Iterator i = getSignatureRequiredArgs(); i.hasNext(); /* */)
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            p_buf.append(arg.getName());
            p_buf.append(":");
            p_buf.append(arg.getType());
            p_buf.append("\n");
        }
        p_buf.append("Optional\n");
        TreeMap optArgs = new TreeMap();
        for (Iterator i = getSignatureOptionalArgs(); i.hasNext(); /* */)
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            optArgs.put(arg.getName(), arg);
        }
        for (Iterator i = optArgs.values().iterator(); i.hasNext(); /* */)
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            p_buf.append(arg.getName());
            p_buf.append(":");
            p_buf.append(arg.getType());
            p_buf.append("\n");
        }
    }
}
