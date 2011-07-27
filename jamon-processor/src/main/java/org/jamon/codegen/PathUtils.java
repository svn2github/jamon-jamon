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

package org.jamon.codegen;

import org.jamon.util.StringUtils;

/**
 * Provides methods to translate template paths to the corresponding interface class name,
 * implementation class name, and the package name for each.
 **/

public class PathUtils {
  private PathUtils() {}

  /**
   * Given a template path, return the name of the interface class for that template.
   *
   * @param path the template path
   * @return the name of the interface class
   **/

  public static String getIntfClassName(final String path) {
    int i = path.lastIndexOf('/');
    return i < 0
        ? path
        : path.substring(i + 1);
  }

  /**
   * Given a template path, return the name of the implementation class for that template.
   *
   * @param p_path the template path
   * @return the name of the implementation class
   **/

  public static String getImplClassName(final String path) {
    return getIntfClassName(path) + "Impl";
  }

  /**
   * Given a template path, return the name of the package in which the interface class for that
   * template lives.
   *
   * @param path the template path
   * @return the name of the interface class package
   **/

  public static String getIntfPackageName(final String path) {
    int i = path.lastIndexOf('/');
    if (i > 0) {
      return StringUtils.templatePathToClassName(path.substring(0, i));
    }
    else {
      return "";
    }
  }

  /**
   * Given a template path, return the name of the package in which the implementation class for
   * that template lives.
   *
   * @param path the template path
   * @return the name of the implementation class package
   **/

  public static String getImplPackageName(final String path) {
    return getIntfPackageName(path);
  }

  /**
   * Given a template path, return the fully qualified name of the interface class for that
   * template.
   *
   * @param path the template path
   * @return the fully qualified name of the interface class
   **/

  public static String getFullyQualifiedIntfClassName(final String path) {
    return fullyQualify(getIntfPackageName(path), getIntfClassName(path));
  }

  /**
   * Given a template path, return the fully qualified name of the implementation class for that
   * template.
   *
   * @param path the template path
   * @return the fully qualified name of the implementation class
   **/

  public static String getFullyQualifiedImplClassName(final String path) {
    return fullyQualify(getImplPackageName(path), getImplClassName(path));
  }

  /**
   * Given a proxy class, return the corresponding template path.
   *
   * @param class the proxy class
   * @return the corresponding template path
   */
  public static String getPathForProxyClass(Class<?> clazz) {
    return clazz.getName().replace(".", "/");
  }

  /**
   * Fully qualify a class name given a package name and class name.
   *
   * @param pkgName the name of the package
   * @param className the name of the class
   * @return the fully qualified name of the class
   **/

  private static String fullyQualify(final String pkgName, final String className) {
    return "".equals(pkgName)
        ? className
        : (pkgName + "." + className);
  }
}
