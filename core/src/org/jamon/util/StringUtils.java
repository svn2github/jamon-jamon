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

package org.jamon.util;

import java.io.File;
import java.util.Iterator;

public class StringUtils
{
    private StringUtils() { }

    public static String filePathToClassName(String p_path)
    {
        return pathToClassName(p_path, File.separator);
    }

    public static String templatePathToClassName(String p_path)
    {
        return pathToClassName(p_path, "/");
    }

    public static String classToTemplatePath(Class p_class)
    {
        return filePathToTemplatePath(classNameToFilePath(p_class.getName()));
    }

    private static String pathToClassName(String p_path,
                                          String p_fromSep)
    {
        StringBuffer sb = new StringBuffer(p_path);
        final int len = p_fromSep.length();
        while (sb.length() > 0 && p_fromSep.equals(sb.substring(0,len)))
        {
            sb.delete(0,len);
        }
        int j = len;
        for (int i = 0; i < sb.length() - len; ++i)
        {
            if (sb.substring(i,j).equals(p_fromSep))
            {
                sb.replace(i,j,".");
            }
            j++;
        }
        return sb.toString();
    }

    public static String filePathToTemplatePath(String p_path)
    {
        StringBuffer sb = new StringBuffer(p_path);
        final int len = File.separator.length();
        int j = len;
        for (int i = 0; i < sb.length() - len; ++i)
        {
            if (sb.substring(i,j).equals(File.separator))
            {
                sb.replace(i,j,"/");
            }
            j++;
        }
        return sb.toString();
    }

    public static String classNameToFilePath(String p_className)
    {
        StringBuffer sb = new StringBuffer(File.separator);
        sb.append(p_className);
        for (int i = File.separator.length(); i < sb.length(); ++i)
        {
            if (sb.charAt(i) == '.')
            {
                sb.replace(i,i+1,File.separator);
            }
        }
        return sb.toString();
    }

    public static String capitalize(String p_string)
    {
        if (p_string == null)
        {
            return null;
        }
        else
        {
            char [] chars = p_string.toCharArray();
            if (chars.length == 0)
            {
                return p_string;
            }
            else
            {
                chars[0] = Character.toUpperCase(chars[0]);
                return new String(chars);
            }
        }
    }

    private static final char[] HEXCHARS =
    {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    public static String byteArrayToHexString(final byte[] bytes)
    {
	StringBuffer buffer = new StringBuffer(bytes.length * 2);
	for (int i = 0; i < bytes.length; i++)
        {
            buffer.append(HEXCHARS[(bytes[i] & 0xF0) >>  4]);
            buffer.append(HEXCHARS[bytes[i] & 0x0F]);
	}
	return buffer.toString();
    }

    public static String hexify4(int p_int)
    {
        int i = p_int;
        String s = Integer.toHexString(i);
        int pad = 3;
        while (i > 16)
        {
            pad--;
            i /= 16;
        }
        return "000".substring(0,pad) + s;
    }

    public static void commaJoin(StringBuffer p_buf, Iterator p_iter)
    {
        while(p_iter.hasNext())
        {
            p_buf.append((String) p_iter.next());
            if (p_iter.hasNext())
            {
                p_buf.append(", ");
            }
        }
    }
}
