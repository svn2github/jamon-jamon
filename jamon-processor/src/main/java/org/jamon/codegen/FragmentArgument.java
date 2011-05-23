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


public class FragmentArgument extends RequiredArgument
{
    public FragmentArgument(FragmentUnit p_fragmentUnit, org.jamon.api.Location p_location)
    {
        super(p_fragmentUnit.getName(),
              p_fragmentUnit.getFragmentInterfaceName(true),
              p_location);
        m_fragmentUnit = p_fragmentUnit;
    }

    public FragmentUnit getFragmentUnit()
    {
        return m_fragmentUnit;
    }


    private final FragmentUnit m_fragmentUnit;

    @Override
    public String getFullyQualifiedType() {
        if (getFragmentUnit().getParent() instanceof TemplateUnit)
        {
            String templateName = ((TemplateUnit) getFragmentUnit().getParent()).getName();
            return PathUtils.getFullyQualifiedIntfClassName(templateName) + ".Intf." + getType();
        }
        return getType();
    }
}
