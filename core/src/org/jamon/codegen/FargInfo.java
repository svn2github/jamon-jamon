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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

class FargInfo extends AbstractUnitInfo
{
    public FargInfo(String p_name)
    {
        super(p_name);
    }

    public FargInfo(String p_name, Iterator p_argNames, Map p_args)
    {
        super(p_name);
        while (p_argNames.hasNext())
        {
            String name = (String) p_argNames.next();
            addRequiredArg(name, (String) p_args.get(name));
        }
    }

    public void addOptionalArg(String p_name,
                                        String p_type,
                                        String p_default)
    {
        throw new UnsupportedOperationException
            ("Frags cannot have optional arguments");
    }

    public void addFarg(String p_name, String p_type)
    {
        // nor can grafs have garfs...
        throw new UnsupportedOperationException("Frags cannot have fargs");
    }

    public String getFargInterfaceName()
    {
        return "Fragment_" + getName();
    }

}
