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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jamon.ParserErrors;
import org.jamon.node.Location;

public abstract class AbstractInnerUnit extends AbstractUnit
{
    public AbstractInnerUnit(
        String p_name, StatementBlock p_parent, ParserErrors p_errors, Location p_location)
    {
        super(p_name, p_parent, p_errors, p_location);
    }

    @Override public void addOptionalArg(OptionalArgument p_arg)
    {
        m_optionalArgs.add(p_arg);
    }

    public Collection<OptionalArgument> getOptionalArgs()
    {
        return m_optionalArgs;
    }

    public boolean hasOptionalArgs()
    {
        return !m_optionalArgs.isEmpty();
    }

    @Override public void addRequiredArg(RequiredArgument p_arg)
    {
        m_requiredArgs.add(p_arg);
    }

    public Set<OptionalArgument> getOptionalArgsSet()
    {
        return m_optionalArgs;
    }

    public List<RequiredArgument> getRequiredArgs()
    {
        return m_requiredArgs;
    }

    public List<RequiredArgument> getDeclaredRequiredArgs()
    {
        return m_requiredArgs;
    }

    public boolean hasRequiredArgs()
    {
        return ! m_requiredArgs.isEmpty();
    }

    @Override protected void addFragmentArg(
        FragmentArgument p_arg)
    {
        m_fragmentArgs.add(p_arg);
    }

    @Override public List<FragmentArgument> getFragmentArgs()
    {
        return m_fragmentArgs;
    }

    @Override public List<AbstractArgument> getRenderArgs()
    {
        return new SequentialList<AbstractArgument>(
                getDeclaredRequiredArgs(),
                new ArrayList<AbstractArgument>(getOptionalArgs()),
                getFragmentArgs());
    }

    @Override public Collection<AbstractArgument> getVisibleArgs()
    {
        return getRenderArgs();
    }

    @Override public List<RequiredArgument> getSignatureRequiredArgs()
    {
        return getRequiredArgs();
    }

    @Override public Collection<OptionalArgument> getSignatureOptionalArgs()
    {
        return getOptionalArgs();
    }



    private final List<RequiredArgument> m_requiredArgs =
        new LinkedList<RequiredArgument>();
    private final Set<OptionalArgument> m_optionalArgs =
        new HashSet<OptionalArgument>();
    private final List<FragmentArgument> m_fragmentArgs =
        new LinkedList<FragmentArgument>();
}
