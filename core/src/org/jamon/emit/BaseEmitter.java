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

package org.jamon.emit;

class BaseEmitter
{
    protected BaseEmitter() {} // avoid public instantiation

    public static String valueOf(int p_int)
    {
        return String.valueOf(p_int);
    }

    public static String valueOf(double p_double)
    {
        return String.valueOf(p_double);
    }

    public static String valueOf(char p_char)
    {
        return String.valueOf(p_char);
    }

    public static String valueOf(boolean p_bool)
    {
        return String.valueOf(p_bool);
    }

    public static String valueOf(float p_float)
    {
        return String.valueOf(p_float);
    }

    public static String valueOf(short p_short)
    {
        return String.valueOf(p_short);
    }

    public static String valueOf(long p_long)
    {
        return String.valueOf(p_long);
    }

    public static String valueOf(byte p_byte)
    {
        return String.valueOf(p_byte);
    }
}
