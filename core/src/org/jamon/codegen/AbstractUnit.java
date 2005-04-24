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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.emit.EmitMode;
import org.jamon.node.ArgNode;
import org.jamon.node.FragmentArgsNode;
import org.jamon.node.Location;
import org.jamon.node.OptionalArgNode;

public abstract class AbstractUnit
    implements Unit
{
    public AbstractUnit(String p_name, Unit p_parent, ParserErrors p_errors)
    {
        m_name = p_name;
        m_parent = p_parent;
        m_errors = p_errors;
    }

    public final String getName()
    {
        return m_name;
    }

    public final Unit getParent()
    {
        return m_parent;
    }
    
    protected final ParserErrors getErrors()
    {
        return m_errors;
    }

    protected abstract void addFragmentArg(FragmentArgument p_arg, Location p_location);
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

    public void generateRenderBody(CodeWriter p_writer,
                                   TemplateDescriber p_describer,
                                   EmitMode p_emitMode) throws ParserError
    {
        p_writer.openBlock();
        printStatements(p_writer, p_describer, p_emitMode);
        printRenderBodyEnd(p_writer);
        p_writer.closeBlock();
    }

    private void printStatements(CodeWriter p_writer,
                                 TemplateDescriber p_describer,
                                 EmitMode p_emitMode) throws ParserError
    {
        for (Iterator i = getStatements().iterator(); i.hasNext(); )
        {
            ((Statement)i.next()).generateSource(p_writer,
                                                 p_describer,
                                                 p_emitMode);
        }
    }

    protected void printRenderBodyEnd(CodeWriter p_writer)
    {
    }

    public abstract void addRequiredArg(RequiredArgument p_arg);
    public abstract void addOptionalArg(OptionalArgument p_arg);
    public abstract Iterator getSignatureRequiredArgs();
    public abstract Iterator getSignatureOptionalArgs();
    public abstract Iterator getVisibleArgs();

    private final String m_name;
    private final Unit m_parent;
    private final ParserErrors m_errors;
    private final List m_statements = new LinkedList();
    private final Set m_argNames = new HashSet();

    public FragmentUnit addFragment(FragmentArgsNode p_node)
    {
        checkArgName(p_node.getFragmentName(), p_node.getLocation());
        FragmentUnit frag = 
            new FragmentUnit(p_node.getFragmentName(), this, m_errors);
        addFragmentArg(new FragmentArgument(frag), p_node.getLocation());
        return frag;
    }

    public void addRequiredArg(ArgNode p_node)
    {
        checkArgName(p_node.getName().getName(), 
                     p_node.getName().getLocation());
        addRequiredArg(new RequiredArgument(p_node));
    }

    public void addOptionalArg(OptionalArgNode p_node)
    {
        checkArgName(p_node.getName().getName(), 
            p_node.getName().getLocation());
        addOptionalArg(new OptionalArgument(p_node));
    }

    protected void addArgName(AbstractArgument p_arg)
    {
        m_argNames.add(p_arg.getName());
    }

    private void checkArgName(String p_name, Location p_location)
    {
        if (! m_argNames.add(p_name))
        {
            getErrors().addError(
                "multiple arguments named " + p_name,
                p_location);
        }
    }

    public Iterator getRenderArgs()
    {
        return new SequentialIterator(getSignatureRequiredArgs(),
                                      getFragmentArgs());
    }

    public void printRenderArgsDecl(CodeWriter p_writer)
    {
        printArgsDecl(p_writer, getRenderArgs());
    }

    public void printRenderArgs(CodeWriter p_writer)
    {
        printArgs(p_writer, getRenderArgs());
    }

    protected static void printArgsDecl(CodeWriter p_writer, Iterator i)
    {
        while (i.hasNext())
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            p_writer.printArg("final " + arg.getType() + " " + arg.getName());
        }
    }

    protected static void printArgs(CodeWriter p_writer, Iterator p_args)
    {
        while (p_args.hasNext())
        {
            p_writer.printArg(((AbstractArgument) p_args.next()).getName());
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
