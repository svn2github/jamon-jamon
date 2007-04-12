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

import org.jamon.node.Location;

public interface MethodUnit extends Unit
{
    String getOptionalArgDefaultMethod(OptionalArgument p_arg);
    Iterator<OptionalArgument> getOptionalArgsWithDefaults();
    String getDefaultForArg(OptionalArgument p_arg);
    boolean isAbstract();
    boolean isOverride();
    Location getLocation();
}
