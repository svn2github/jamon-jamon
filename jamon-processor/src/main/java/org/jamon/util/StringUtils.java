/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.util;

import java.io.File;
import java.util.regex.Pattern;

public class StringUtils {
  private static final char TEMPLATE_PATH_SEPARATOR_CHAR = '/';
  private static final String TEMPLATE_PATH_SEPARATOR = "/";

  private StringUtils() {}

  /**
   * Return the directory part of a template path, in filesystem format (i.e. using
   * <code>File.separator</code>).
   *
   * @param path the template path
   * @return the name of the directory part of that path
   */
  public static String templatePathToFileDir(String path) {
    int k = path.lastIndexOf(TEMPLATE_PATH_SEPARATOR_CHAR);
    if (k <= 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder(path.substring(0, k));
    int j = 1;
    for (int i = 0; i < sb.length() - 1; ++i) {
      if (sb.substring(i, j).equals(TEMPLATE_PATH_SEPARATOR)) {
        sb.replace(i, j, File.separator);
      }
      j++;
    }
    return sb.toString();
  }

  /**
   * Determine if the given file name could be part of a given class. A file is considered part of a
   * class if it is either the class file for the class, or the class file for an inner class of the
   * class.
   *
   * @param className the class name
   * @param fileName the file name
   * @return true if the named file is part of a class
   */
  public static boolean isGeneratedClassFilename(String className, String fileName) {
    if (fileName.endsWith(".class")) {
      if (fileName.startsWith(className)) {
        String rest = fileName.substring(className.length());
        if (rest.equals(".class") || rest.charAt(0) == '$') {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Convert a /-separated path into a class name.
   *
   * @param path the /-separated path
   * @return the class name
   */
  public static String templatePathToClassName(String path) {
    StringBuilder sb = new StringBuilder(path);
    while (sb.length() > 0 && TEMPLATE_PATH_SEPARATOR_CHAR == sb.charAt(0)) {
      sb.delete(0, 1);
    }
    int j = 1;
    for (int i = 0; i < sb.length() - 1; ++i) {
      if (sb.substring(i, j).equals(TEMPLATE_PATH_SEPARATOR)) {
        sb.replace(i, j, ".");
      }
      j++;
    }
    return sb.toString();
  }

  /**
   * Compute the /-separated path for a given class.
   *
   * @param clazz the class
   * @return the /-separated path
   */
  public static String classToTemplatePath(Class<?> clazz) {
    return filePathToTemplatePath(classNameToFilePath(clazz.getName()));
  }

  /**
   * Convert a path using the systems path separator into a /-separated path.
   *
   * @param path an OS-specific path
   * @return a /-separated path
   */
  public static String filePathToTemplatePath(String path) {
    return path.replaceAll(Pattern.quote(File.separator), TEMPLATE_PATH_SEPARATOR);
  }

  /**
   * Construct the relative path to a class file on the file system for a given class, without the
   * ".class" suffix.
   *
   * @param className the class name
   * @return a relative path to the class file on the native file system
   */
  public static String classNameToFilePath(String className) {
    StringBuilder sb = new StringBuilder(File.separator);
    sb.append(className);
    for (int i = File.separator.length(); i < sb.length(); ++i) {
      if (sb.charAt(i) == '.') {
        sb.replace(i, i + 1, File.separator);
      }
    }
    return sb.toString();
  }

  /**
   * Capitalize the first letter of a string. If the string is empty, just return an empty string;
   * if it is null, return a null string. string.
   *
   * @param string the string
   * @return {@code p_string} with the first letter capitalized
   */
  public static String capitalize(String string) {
    if (string == null) {
      return null;
    }
    else if (string.length() == 0) {
      return string;
    }
    else {
      char[] chars = string.toCharArray();
      chars[0] = Character.toUpperCase(chars[0]);
      return new String(chars);
    }
  }

  private static final char[] HEXCHARS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  /**
   * Convert a byte array into a hexidecimal representation.
   *
   * @param bytes the byte array
   * @return a hexidecimal representation
   */
  public static String byteArrayToHexString(final byte[] bytes) {
    StringBuilder buffer = new StringBuilder(bytes.length * 2);
    for (int i = 0; i < bytes.length; i++) {
      buffer.append(HEXCHARS[(bytes[i] & 0xF0) >> 4]);
      buffer.append(HEXCHARS[bytes[i] & 0x0F]);
    }
    return buffer.toString();
  }

  /**
   * Compute the 4-digit hexidecimal represenation of an integer between {@code 0} and {@code
   * 0xFFFF}.
   *
   * @param integer the integer
   * @return the 4-digit hexidecimal represenation
   */
  public static String hexify4(int integer) {
    int i = integer;
    String s = Integer.toHexString(i);
    int pad = 3;
    while (i > 16) {
      pad--;
      i /= 16;
    }
    return "000".substring(0, pad) + s;
  }

  /**
   * Concatenate a list of strings into a single {@code StringBuilder}, with commas between
   * successive elements.
   *
   * @param builder the builder
   * @param iterable the list of strings
   */
  public static void commaJoin(StringBuilder builder, Iterable<String> iterable) {
    boolean seenElement = false;
    for (String element : iterable) {
      if (seenElement) {
        builder.append(", ");
      }
      else {
        seenElement = true;
      }
      builder.append(element);
    }
  }
}
