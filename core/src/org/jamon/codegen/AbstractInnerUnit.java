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

public abstract class AbstractInnerUnit extends AbstractUnit
{
    public AbstractInnerUnit(String p_name, Unit p_parent)
    {
        super(p_name, p_parent);
    }

    public void addOptionalArg(OptionalArgument p_arg)
    {
        checkArgName(p_arg);
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

    public void addRequiredArg(RequiredArgument p_arg)
    {
        checkArgName(p_arg);
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

    public void addFragmentArg(FragmentArgument p_arg)
    {
        checkArgName(p_arg);
        m_fragmentArgs.add(p_arg);
    }

    public Iterator getFragmentArgs()
    {
        return m_fragmentArgs.iterator();
    }

    public List getFragmentArgsList()
    {
        return m_fragmentArgs;
    }

    public Iterator getRenderArgs()
    {
        return new SequentialIterator(getDeclaredRequiredArgs(),
                                      getOptionalArgs(),
                                      getFragmentArgs());
    }

    public Iterator getVisibleArgs()
    {
        return getRenderArgs();
    }

    public Iterator getSignatureRequiredArgs()
    {
        return getRequiredArgs();
    }

    public Iterator getSignatureOptionalArgs()
    {
        return getOptionalArgs();
    }



    private final List m_requiredArgs = new LinkedList();
    private final Set m_optionalArgs = new HashSet();
    private final List m_fragmentArgs = new LinkedList();
}
