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
package org.jamon.eclipse;

import org.eclipse.core.resources.IFile;
import org.jamon.TemplateLocation;

public class ResourceTemplateLocation implements TemplateLocation
{
    public ResourceTemplateLocation(IFile p_file)
    {
        m_file = p_file;
    }
    
    @Override public String toString()
    {
        return m_file.getFullPath().toString();
    }
    
    public IFile getFile()
    {
        return m_file;
    }

    private final IFile m_file;
}
