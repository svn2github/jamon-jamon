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
 * Contributor(s):
 */

package org.jamon.emit;

public final class LimitedEmitter
    extends BaseEmitter
{
    private LimitedEmitter()
    {
        // non instantiable
    }

    public static String valueOf(Integer p_value)
    {
        return StandardEmitter.valueOf(p_value);
    }

    public static String valueOf(Double p_value)
    {
        return StandardEmitter.valueOf(p_value);
    }

    public static String valueOf(Character p_value)
    {
        return StandardEmitter.valueOf(p_value);
    }

    public static String valueOf(Boolean p_value)
    {
        return StandardEmitter.valueOf(p_value);
    }

    public static String valueOf(Float p_value)
    {
        return StandardEmitter.valueOf(p_value);
    }

    public static String valueOf(Short p_value)
    {
        return StandardEmitter.valueOf(p_value);
    }

    public static String valueOf(Long p_value)
    {
        return StandardEmitter.valueOf(p_value);
    }

    public static String valueOf(Byte p_value)
    {
        return StandardEmitter.valueOf(p_value);
    }

    public static String valueOf(String p_string)
    {
        return StrictEmitter.valueOf(p_string);
    }
}
