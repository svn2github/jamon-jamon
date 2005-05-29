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

import org.jamon.node.Location;

public class MethodCallStatement
    extends AbstractInnerUnitCallStatement
{
    MethodCallStatement(String p_path,
                        ParamValues p_params,
                        MethodUnit p_methodUnit,
                        Location p_location,
                        String p_templateIdentifier)
    {
        super(p_path, p_params, p_methodUnit, p_location, p_templateIdentifier);
    }

    @Override protected String getDefault(OptionalArgument p_arg)
    {
        return ((MethodUnit) getUnit())
            .getOptionalArgDefaultMethod(p_arg)
            + "()";
    }
}
