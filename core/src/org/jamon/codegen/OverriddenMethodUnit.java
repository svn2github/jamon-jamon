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

import org.jamon.node.AArg;
import org.jamon.node.AParentArg;

public class OverriddenMethodUnit
    extends AbstractUnit
    implements MethodUnit, InheritedUnit
{
    public OverriddenMethodUnit(DeclaredMethodUnit p_declaredMethodUnit,
                                Unit p_parent)
    {
        super(p_declaredMethodUnit.getName(), p_parent);
        m_declaredMethodUnit = p_declaredMethodUnit;
        m_inheritedArgs =
            new InheritedArgs(getName(),
                              getName(),
                              p_declaredMethodUnit.getRequiredArgsList(),
                              p_declaredMethodUnit.getOptionalArgsSet(),
                              p_declaredMethodUnit.getFragmentArgsList());

    }

    public void addParentArg(AParentArg p_arg)
    {
        m_inheritedArgs.addParentArg(p_arg);
    }

    public Iterator getVisibleArgs()
    {
        return m_inheritedArgs.getVisibleArgs();
    }

    private final DeclaredMethodUnit m_declaredMethodUnit;
    private final InheritedArgs m_inheritedArgs;

    public Iterator getFragmentArgs()
    {
        return m_declaredMethodUnit.getFragmentArgs();
    }

    public Iterator getSignatureRequiredArgs()
    {
        return m_declaredMethodUnit.getSignatureRequiredArgs();
    }

    public Iterator getSignatureOptionalArgs()
    {
        return m_declaredMethodUnit.getSignatureOptionalArgs();
    }

    public String getOptionalArgDefaultMethod(OptionalArgument p_arg)
    {
        return m_declaredMethodUnit.getOptionalArgDefaultMethod(p_arg);
    }

    public void printRenderArgsDecl(CodeWriter p_writer)
    {
        for (Iterator i = m_declaredMethodUnit.getRenderArgs(); i.hasNext(); )
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            p_writer.printArg("final " + arg.getType() + " "
                              + (m_inheritedArgs.isArgVisible(arg)
                                 ? "" : "p__jamon__" )
                              + arg.getName());
        }
    }

    public boolean isAbstract()
    {
        return false;
    }


    public void addFragmentArg(org.jamon.codegen.FragmentArgument p_arg)
    {
        throw new UnsupportedOperationException();
    }

    public List getFragmentArgsList()
    {
        throw new UnsupportedOperationException();
    }

    public void addRequiredArg(org.jamon.codegen.RequiredArgument p_arg)
    {
        throw new UnsupportedOperationException();
    }

    public void addOptionalArg(org.jamon.codegen.OptionalArgument p_arg)
    {
        throw new UnsupportedOperationException();
    }
}
