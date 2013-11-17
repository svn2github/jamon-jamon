/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.util;

import java.io.StringWriter;
import java.util.List;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Compiler that uses the Java 6 Compiler API to do compilation in-memory.
 */
public class Java6Compiler implements JavaCompiler {
  private final List<String> compilerArgs;

  private final javax.tools.JavaCompiler javaCompiler;

  private final StandardJavaFileManager javaFileManager;

  /**
   * Constructor for creating a new Java6Compiler.
   *
   * @param compilerArgs the arguments to pass to the compiler.
   */
  public Java6Compiler(List<String> compilerArgs) {
    javaCompiler = ToolProvider.getSystemJavaCompiler();
    javaFileManager = javaCompiler.getStandardFileManager(null, null, null);

    this.compilerArgs = compilerArgs;
  }

  @Override
  public String compile(String[] javaFiles) {
    Iterable<? extends JavaFileObject> fileObjects = javaFileManager.getJavaFileObjects(javaFiles);
    StringWriter stringWriter = new StringWriter();
    if (!javaCompiler.getTask(stringWriter, javaFileManager, null, compilerArgs, null, fileObjects)
        .call()) {
      return stringWriter.toString();
    }
    else {
      return null;
    }
  }
}
