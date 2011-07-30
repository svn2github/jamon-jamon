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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2011 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.jamon.compiler.RecompilingTemplateManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import com.google.common.io.Files;
import com.google.common.io.Resources;

public class JavaCompilerFactoryTest {
  private static final String JAVA_CLASS_NAME = "DummyFileToCompile";

  private static final String JAVA_FILE = JAVA_CLASS_NAME + ".java";

  private RecompilingTemplateManager.Data data;
  private File workDir;
  private ClassLoader workClassLoader;
  private JavaCompiler compiler;

  @Before
  public void setUp() throws Exception {
    data = new RecompilingTemplateManager.Data();
    data.setSourceDir(
      new File(getClass().getClassLoader().getResource(JAVA_FILE).toURI()).getParent());
    workDir = Files.createTempDir();
    data.setWorkDir(workDir.getPath());
    workClassLoader = new URLClassLoader(new URL[] { workDir.toURI().toURL() });
  }

  @After
  public void tearDown() throws Exception {
    if (workDir != null) {
      Files.deleteRecursively(workDir);
    }
  }

  @Test
  public void testJava6Compiler() throws Exception {
    initializeJava6Compiler();
    compileAndVerifyDummyFile();
  }

  @Test
  public void testJava6CompilerWithError() throws Exception {
    initializeJava6Compiler();
    compileFileWithError();
  }

  @Test
  public void testExternalJavaCompiler() throws Exception {
    initializeExternalJavaCompiler();
    compileAndVerifyDummyFile();
  }

  @Test
  public void testExternalJavaCompilerWithError() throws Exception {
    initializeExternalJavaCompiler();
    compileFileWithError();
  }

  private void initializeJava6Compiler() {
    compiler = JavaCompilerFactory.makeCompiler(
      data,
      workDir.getAbsolutePath(),
      getClass().getClassLoader());
    assertEquals(Java6Compiler.class, compiler.getClass());
  }

  private void initializeExternalJavaCompiler() {
    data.setJavaCompiler(JavaCompilerFactory.getDefaultJavac());
    compiler = JavaCompilerFactory.makeCompiler(
      data,
      workDir.getAbsolutePath(),
      getClass().getClassLoader());
    assertEquals(ExternalJavaCompiler.class, compiler.getClass());
  }

  private void compileFileWithError() throws Exception {
    String errors = compiler.compile(toJavaFileList("WithError.java"));
    assertThat(errors, JUnitMatchers.containsString("missing return statement"));
  }

  private void compileAndVerifyDummyFile() throws Exception {
    String errors = compiler.compile(toJavaFileList(JAVA_FILE));
    assertNull(errors, errors);

    Class<?> clazz = workClassLoader.loadClass(JAVA_CLASS_NAME);
    assertEquals("success", clazz.getDeclaredMethod("foo").invoke(null));
  }

  private String[] toJavaFileList(String javaFile) throws IOException {
    File destFile = new File(workDir, javaFile);
    Files.copy(
      Resources.newInputStreamSupplier(getClass().getClassLoader().getResource(javaFile)),
      destFile);
    return new String[] { destFile.getAbsolutePath() };
  }
}
