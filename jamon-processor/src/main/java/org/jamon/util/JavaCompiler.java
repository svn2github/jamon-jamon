/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.util;

/**
 * A <code>JavaCompiler</code> is an object which knows how to compile Java source files.
 */
public interface JavaCompiler {
  /**
   * Compile the specified java source files.
   *
   * @param javaFiles the source files to compile
   * @return null if the compilation was successful, otherwise a description of the failure
   */
  String compile(String[] javaFiles);
}
