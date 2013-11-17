/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
