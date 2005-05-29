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

import org.jamon.ParserErrors;
import org.jamon.node.Location;

public abstract class AbstractInnerUnit extends AbstractUnit
{
    public AbstractInnerUnit(
        String p_name, Unit p_parent, ParserErrors p_errors)
    {
        super(p_name, p_parent, p_errors);
    }

    @Override public void addOptionalArg(OptionalArgument p_arg)
    {
        m_optionalArgs.add(p_arg);
    }

    public Iterator getOptionalArgs()
    {
        return m_optionalArgs.iterator();
    }

    public boolean hasOptionalArgs()
    {
        return !m_optionalArgs.isEmpty();
    }

    @Override public void addRequiredArg(RequiredArgument p_arg)
    {
        m_requiredArgs.add(p_arg);
    }

    public List getRequiredArgsList()
    {
        return m_requiredArgs;
    }

    public Set getOptionalArgsSet()
    {
        return m_optionalArgs;
    }

    public Iterator getRequiredArgs()
    {
        return getRequiredArgsList().iterator();
    }

    public Iterator getDeclaredRequiredArgs()
    {
        return m_requiredArgs.iterator();
    }

    public boolean hasRequiredArgs()
    {
        return ! getRequiredArgsList().isEmpty();
    }

    @Override protected void addFragmentArg(FragmentArgument p_arg, Location p_location)
    {
        m_fragmentArgs.add(p_arg);
    }

    @Override public Iterator getFragmentArgs()
    {
        return m_fragmentArgs.iterator();
    }

    @Override public List getFragmentArgsList()
    {
        return m_fragmentArgs;
    }

    @Override public Iterator getRenderArgs()
    {
        return new SequentialIterator(getDeclaredRequiredArgs(),
                                      getOptionalArgs(),
                                      getFragmentArgs());
    }

    @Override public Iterator getVisibleArgs()
    {
        return getRenderArgs();
    }

    @Override public Iterator getSignatureRequiredArgs()
    {
        return getRequiredArgs();
    }

    @Override public Iterator getSignatureOptionalArgs()
    {
        return getOptionalArgs();
    }



    private final List m_requiredArgs = new LinkedList();
    private final Set m_optionalArgs = new HashSet();
    private final List m_fragmentArgs = new LinkedList();
}
