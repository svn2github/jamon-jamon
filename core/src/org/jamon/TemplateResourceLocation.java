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
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */
package org.jamon;

public class TemplateResourceLocation implements TemplateLocation
{
    public TemplateResourceLocation(String p_location)
    {
        m_location = p_location;
    }
    
    @Override public String toString() 
    { 
        return m_location; 
    }
    
    @Override public boolean equals(Object p_obj)
    {
        return p_obj instanceof TemplateResourceLocation
            && m_location.equals(((TemplateResourceLocation) p_obj).m_location);
    }
    
    @Override public int hashCode()
    {
        return m_location.hashCode();
    }
    
    private final String m_location;

}
