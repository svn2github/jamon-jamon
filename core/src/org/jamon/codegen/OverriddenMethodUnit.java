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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import java.util.Iterator;
import java.util.List;

import org.jamon.ParserErrors;
import org.jamon.node.ParentArgNode;

public class OverriddenMethodUnit
    extends AbstractUnit
    implements MethodUnit, InheritedUnit
{
    public OverriddenMethodUnit(DeclaredMethodUnit p_declaredMethodUnit,
                                Unit p_parent,
                                ParserErrors p_errors)
    {
        super(p_declaredMethodUnit.getName(), p_parent, p_errors);
        m_declaredMethodUnit = p_declaredMethodUnit;
        m_inheritedArgs =
            new InheritedArgs(getName(),
                              getName(),
                              p_declaredMethodUnit.getRequiredArgsList(),
                              p_declaredMethodUnit.getOptionalArgsSet(),
                              p_declaredMethodUnit.getFragmentArgsList(),
                              p_errors);

    }

    public void addParentArg(ParentArgNode p_node)
    {
        m_inheritedArgs.addParentArg(p_node);
    }

    @Override public Iterator<AbstractArgument> getVisibleArgs()
    {
        return m_inheritedArgs.getVisibleArgs();
    }

    private final DeclaredMethodUnit m_declaredMethodUnit;
    private final InheritedArgs m_inheritedArgs;

    @Override public Iterator<FragmentArgument> getFragmentArgs()
    {
        return m_declaredMethodUnit.getFragmentArgs();
    }

    @Override public Iterator<RequiredArgument> getSignatureRequiredArgs()
    {
        return m_declaredMethodUnit.getSignatureRequiredArgs();
    }

    @Override public Iterator<OptionalArgument> getSignatureOptionalArgs()
    {
        return m_declaredMethodUnit.getSignatureOptionalArgs();
    }

    public String getOptionalArgDefaultMethod(OptionalArgument p_arg)
    {
        return m_declaredMethodUnit.getOptionalArgDefaultMethod(p_arg);
    }

    @Override public void printRenderArgsDecl(CodeWriter p_writer)
    {
        for (Iterator<AbstractArgument> i = m_declaredMethodUnit.getRenderArgs();
             i.hasNext(); )
        {
            AbstractArgument arg = i.next();
            p_writer.printListElement("final " + arg.getType() + " "
                              + (m_inheritedArgs.isArgVisible(arg)
                                 ? "" : "p__jamon__" )
                              + arg.getName());
        }
    }

    public boolean isAbstract()
    {
        return false;
    }


    @Override public void addFragmentArg(org.jamon.codegen.FragmentArgument p_arg)
    {
        throw new UnsupportedOperationException();
    }

    @Override public List<FragmentArgument> getFragmentArgsList()
    {
        throw new UnsupportedOperationException();
    }

    @Override public void addRequiredArg(org.jamon.codegen.RequiredArgument p_arg)
    {
        throw new UnsupportedOperationException();
    }

    @Override public void addOptionalArg(org.jamon.codegen.OptionalArgument p_arg)
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<OptionalArgument> getOptionalArgsWithDefaults()
    {
        return m_inheritedArgs.getOptionalArgsWithNewDefaultValues();
    }

    public String getDefaultForArg(OptionalArgument p_arg)
    {
        return m_inheritedArgs.getDefaultValue(p_arg);
    }

    public boolean isOverride()
    {
        return true;
    }
}
