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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.ArgNode;
import org.jamon.node.FragmentArgsNode;
import org.jamon.node.OptionalArgNode;

public abstract class AbstractUnit
    extends AbstractStatementBlock
    implements Unit
{
    public AbstractUnit(
        String p_name, StatementBlock p_parent, ParserErrorsImpl p_errors, org.jamon.api.Location p_location)
    {
        super(p_parent, p_location);
        m_name = p_name;
        m_errors = p_errors;
    }

    public final String getName()
    {
        return m_name;
    }

    @Override public final Unit getParentUnit()
    {
        return (Unit) getParent();
    }

    protected final ParserErrorsImpl getErrors()
    {
        return m_errors;
    }

    protected abstract void addFragmentArg(FragmentArgument p_arg);
    public abstract List<FragmentArgument> getFragmentArgs();

    @Override public FragmentUnit getFragmentUnitIntf(String p_path)
    {
        for (FragmentArgument arg: getFragmentArgs())
        {
            if (p_path.equals(arg.getName()))
            {
                return arg.getFragmentUnit();
            }
        }
        return null;
    }

    public void generateRenderBody(CodeWriter p_writer,
                                   TemplateDescriber p_describer) throws ParserErrorImpl
    {
        p_writer.openBlock();
        printStatements(p_writer, p_describer);
        printRenderBodyEnd(p_writer);
        p_writer.closeBlock();
    }

    protected void printRenderBodyEnd(@SuppressWarnings("unused") CodeWriter p_writer)
    {
    }

    public abstract void addRequiredArg(RequiredArgument p_arg);
    public abstract void addOptionalArg(OptionalArgument p_arg);
    public abstract List<RequiredArgument> getSignatureRequiredArgs();
    public abstract Collection<OptionalArgument> getSignatureOptionalArgs();
    public abstract Collection<AbstractArgument> getVisibleArgs();

    private final String m_name;
    private final ParserErrorsImpl m_errors;
    private final Set<String> m_argNames = new HashSet<String>();
    @Override public FragmentUnit addFragment(
        FragmentArgsNode p_node, GenericParams p_genericParams)
    {
        checkArgName(p_node.getFragmentName(), p_node.getLocation());
        FragmentUnit frag = new FragmentUnit(
            p_node.getFragmentName(), this, p_genericParams, m_errors, p_node.getLocation());
        addFragmentArg(new FragmentArgument(frag, p_node.getLocation()));
        return frag;
    }

    @Override public void addRequiredArg(ArgNode p_node)
    {
        checkArgName(p_node.getName().getName(),
                     p_node.getName().getLocation());
        addRequiredArg(new RequiredArgument(p_node));
    }

    @Override public void addOptionalArg(OptionalArgNode p_node)
    {
        checkArgName(p_node.getName().getName(),
            p_node.getName().getLocation());
        addOptionalArg(new OptionalArgument(p_node));
    }

    protected void addArgName(AbstractArgument p_arg)
    {
        m_argNames.add(p_arg.getName());
    }

    private void checkArgName(String p_name, org.jamon.api.Location p_location)
    {
        if (! m_argNames.add(p_name))
        {
            getErrors().addError(
                "multiple arguments named " + p_name,
                p_location);
        }
    }

    public List<AbstractArgument> getRenderArgs()
    {
        return new SequentialList<AbstractArgument>(
                getSignatureRequiredArgs(),
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

    protected static void printArgsDecl(
        CodeWriter p_writer, Iterable<? extends AbstractArgument> i)
    {
        for (AbstractArgument arg: i)
        {
            p_writer.printListElement("final " + arg.getType() + " " + arg.getName());
        }
    }

    protected static void printArgs(
        CodeWriter p_writer, Iterable<? extends AbstractArgument> p_args)
    {
        for (AbstractArgument arg: p_args)
        {
            p_writer.printListElement(arg.getName());
        }
    }

    protected void generateInterfaceSummary(StringBuilder p_buf)
    {
        p_buf.append("Required\n");
        for (AbstractArgument arg: getSignatureRequiredArgs())
        {
            p_buf.append(arg.getName());
            p_buf.append(":");
            p_buf.append(arg.getType());
            p_buf.append("\n");
        }
        p_buf.append("Optional\n");
        TreeMap<String, OptionalArgument> optArgs =
            new TreeMap<String, OptionalArgument>();
        for (OptionalArgument arg: getSignatureOptionalArgs())
        {
            optArgs.put(arg.getName(), arg);
        }
        for (OptionalArgument arg : optArgs.values())
        {
            p_buf.append(arg.getName());
            p_buf.append(":");
            p_buf.append(arg.getType());
            p_buf.append("\n");
        }
    }
}
