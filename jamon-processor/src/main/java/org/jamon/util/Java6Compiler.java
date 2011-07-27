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
 * The Initial Developer of the Original Code is Matt Raible.  Portions
 * created by Matt Raible are Copyright (C) 2003 Matt Raible.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

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
