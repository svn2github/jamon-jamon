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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FragmentUnit extends AbstractInnerUnit
{
    public FragmentUnit(String p_name, Unit p_parent)
    {
        super(p_name, p_parent);
    }

    public String getFragmentInterfaceName()
    {
        if(getParent() instanceof DefUnit)
        {
            return "Fragment_" + getParent().getName()
                + "__jamon__" + getName();
        }
        else
        {
            return "Fragment_" + getName();
        }
    }

    public void addFragmentArg(FragmentArgument p_arg)
    {
        throw new TunnelingException
            ("fragment '" + getName() + "' has fragment argument(s)");
    }

    public void addOptionalArg(OptionalArgument p_arg)
    {
        throw new TunnelingException
            ("fragment '" + getName() + "' has optional argument(s)");
    }

    public FragmentUnit getFragmentUnitIntf(String p_path)
    {
        return getParent().getFragmentUnitIntf(p_path);
    }
}
