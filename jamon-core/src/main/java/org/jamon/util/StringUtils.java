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

package org.jamon.util;

import java.io.File;

public class StringUtils
{
    private static final char TEMPLATE_PATH_SEPARATOR_CHAR = '/';
    private static final String TEMPLATE_PATH_SEPARATOR = "/";


    private StringUtils() {}

    /**
     * Return the directory part of a template path, in filesystem
     * format (i.e. using <code>File.separator</code>).
     *
     * @param p_path the template path
     *
     * @return the name of the directory part of that path
     */
    public static String templatePathToFileDir(String p_path)
    {
        int k = p_path.lastIndexOf(TEMPLATE_PATH_SEPARATOR_CHAR);
        if (k <= 0)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder(p_path.substring(0,k));
        int j = 1;
        for (int i = 0; i < sb.length() - 1; ++i)
        {
            if (sb.substring(i,j).equals(TEMPLATE_PATH_SEPARATOR))
            {
                sb.replace(i,j,File.separator);
            }
            j++;
        }
        return sb.toString();
    }

    /**
     * Determine if the given file name could be part of a given class.  A file is considered
     * part of a class if it is either the class file for the class, or the class file for an inner
     * class of the class.
     *
     * @param p_className the class name
     * @param p_fileName the file name
     * @return true if the named file is part of a class
     */
    public static boolean isGeneratedClassFilename(String p_className, String p_fileName)
    {
        if (p_fileName.endsWith(".class"))
        {
            if (p_fileName.startsWith(p_className))
            {
                String rest = p_fileName.substring(p_className.length());
                if (rest.equals(".class") || rest.charAt(0) == '$')
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Convert a /-separated path into a class name.
     * @param p_path the /-separated path
     * @return the class name
     */
    public static String templatePathToClassName(String p_path)
    {
        StringBuilder sb = new StringBuilder(p_path);
        while (sb.length() > 0 && TEMPLATE_PATH_SEPARATOR_CHAR == sb.charAt(0))
        {
            sb.delete(0,1);
        }
        int j = 1;
        for (int i = 0; i < sb.length() - 1; ++i)
        {
            if (sb.substring(i,j).equals(TEMPLATE_PATH_SEPARATOR))
            {
                sb.replace(i,j,".");
            }
            j++;
        }
        return sb.toString();
    }


    /**
     * Compute the /-separated path for a given class.
     * @param p_class the class
     * @return the /-separated path
     */
    public static String classToTemplatePath(Class<?> p_class)
    {
        return filePathToTemplatePath(classNameToFilePath(p_class.getName()));
    }

    /**
     * Convert a path using the systems path separator into a /-separated path.
     * @param p_path an OS-specific path
     * @return a /-separated path
     */
    public static String filePathToTemplatePath(String p_path)
    {
        return p_path.replaceAll(File.separator, TEMPLATE_PATH_SEPARATOR);
    }

    /**
     * Construct the relative path to a class file on the file system for a given class, without
     * the ".class" suffix.
     * @param p_className the class name
     * @return a relative path to the class file on the native file system
     */
    public static String classNameToFilePath(String p_className)
    {
        StringBuilder sb = new StringBuilder(File.separator);
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


    /**
     * Capitalize the first letter of a string.  If the string is empty, just return an empty
     * string; if it is null, return a null string.
     * string.
     * @param p_string the string
     * @return {@code p_string} with the first letter capitalized
     */
    public static String capitalize(String p_string)
    {
        if (p_string == null)
        {
            return null;
        }
        else if (p_string.length() == 0) {
            return p_string;
        }
        else
        {
            char [] chars = p_string.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        }
    }

    private static final char[] HEXCHARS =
    {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };


    /**
     * Convert a byte array into a hexidecimal representation.
     * @param bytes the byte array
     * @return a hexidecimal representation
     */
    public static String byteArrayToHexString(final byte[] bytes)
    {
        StringBuilder buffer = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++)
        {
            buffer.append(HEXCHARS[(bytes[i] & 0xF0) >>  4]);
            buffer.append(HEXCHARS[bytes[i] & 0x0F]);
    }
    return buffer.toString();
    }

    /**
     * Compute the 4-digit hexidecimal represenation of an integer between {@code 0} and
     * {@code 0xFFFF}.
     * @param p_int the integer
     * @return the 4-digit hexidecimal represenation
     */
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

    /**
     * Concatenate a list of strings into a single {@code StringBuilder}, with commas between
     * successive elements.
     * @param p_builder the builder
     * @param p_iterable the list of strings
     */
    public static void commaJoin(StringBuilder p_builder, Iterable<String> p_iterable)
    {
        boolean seenElement = false;
        for(String element: p_iterable)
        {
            if (seenElement)
            {
                p_builder.append(", ");
            }
            else
            {
                seenElement = true;
            }
            p_builder.append(element);
        }
    }
}
