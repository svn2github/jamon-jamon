/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jamon.Renderer;
import org.jamon.TemplateManager;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.compiler.RecompilingTemplateManager;
import org.jamon.compiler.TemplateFileLocation;
import org.jamon.compiler.TemplateProcessor;
import org.jamon.node.LocationImpl;

public abstract class TestBase extends TestCase {
  @Override
  public void setUp() throws Exception {
    recompilingTemplateManager = null;
    resetWriter();
  }

  private static final File TEMPLATE_SOURCE_DIR = getTemplateSourceFile();

  protected static final String SOURCE_DIR = TEMPLATE_SOURCE_DIR.getAbsolutePath();

  protected static final String WORK_DIR =
    TEMPLATE_SOURCE_DIR.getParentFile().getParent() + "/workdir";

  private static File getTemplateSourceFile() {
    try {
      return new File(
        new File(TestBase.class.getProtectionDomain().getCodeSource().getLocation() .toURI()),
        "templates");
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected void resetWriter() {
    writer = new StringWriter();
  }

  protected Writer getWriter() {
    return writer;
  }

  protected void checkOutputContains(String expected) {
    assertTrue("output doesn't contain: \"" + expected + "\"", getOutput().indexOf(expected) >= 0);
  }

  protected void checkOutput(String expected) {
    assertEquals(expected, getOutput());
  }

  protected void checkOutput(Renderer renderer, String expected) throws IOException {
    renderer.renderTo(getWriter());
    assertEquals(expected, getOutput());
  }

  protected void checkOutput(String message, String expected) {
    assertEquals(message, expected, getOutput());
  }

  protected TemplateManager getRecompilingTemplateManager() {
    if (recompilingTemplateManager == null) {
      recompilingTemplateManager = constructRecompilingTemplateManager();
    }
    return recompilingTemplateManager;
  }

  private TemplateManager constructRecompilingTemplateManager() {
    return new RecompilingTemplateManager(
      new RecompilingTemplateManager.Data().setSourceDir(SOURCE_DIR).setWorkDir(WORK_DIR));
  }

  protected void clearWorkDir() throws IOException {
    FileUtils.cleanDirectory(new File(WORK_DIR));
  }

  private String removeCrs(CharSequence string) {
    StringBuilder buffer = new StringBuilder(string.length());
    for (int i = 0; i < string.length(); ++i) {
      char c = string.charAt(i);
      if (c != '\r') {
        buffer.append(c);
      }
    }
    return buffer.toString();
  }

  protected String getOutput() {
    writer.flush();
    return removeCrs(writer.getBuffer());
  }

  /**
   * Run the processor on a template file present in the classpath; this is typically used for
   * processing templates which are expected to have an error in them.
   *
   * @param path
   * @throws Exception
   */
  protected void generateSource(String path) throws Exception {
    new TemplateProcessor(
      new File(WORK_DIR + File.separator + "src"),
      TEMPLATE_SOURCE_DIR,
      getClass().getClassLoader())
    .generateSource(path);
  }

  protected static class PartialError {
    private final String message;

    private final int line, column;

    public PartialError(final String message, final int line, final int column) {
      this.message = message;
      this.line = line;
      this.column = column;
    }

    public ParserErrorImpl makeError(String path) {
      return new ParserErrorImpl(
        new LocationImpl(new TemplateFileLocation(getTemplateFilePath(path)), line, column),
        message);
    }
  }

  protected void expectParserErrors(String path, PartialError... partialErrors) throws Exception {
    String fullPath = "test/jamon/broken/" + path;
    try {
      generateSource(fullPath);
      fail();
    }
    catch (ParserErrorsImpl e) {
      List<ParserErrorImpl> expected = new ArrayList<ParserErrorImpl>(partialErrors.length);
      for (PartialError partialError : partialErrors) {
        expected.add(partialError.makeError(fullPath));
      }

      assertEquals(expected, e.getErrors());
    }

  }

  protected void expectParserError(String path, String message, int line, int column)
  throws Exception {
    expectParserErrors(path, new PartialError(message, line, column));
  }

  private static String getTemplateFilePath(String path) {
    return TEMPLATE_SOURCE_DIR.getAbsolutePath() + "/" + path + ".jamon";
  }

  public static void assertEquals(String first, String second) {
    if (showFullContextWhenStringEqualityFails()) {
      assertEquals((Object) first, (Object) second);
    }
    else {
      Assert.assertEquals(first, second);
    }
  }

  private static boolean showFullContextWhenStringEqualityFails() {
    return Boolean.valueOf(System.getProperty("org.jamon.integration.verbose", "false"))
        .booleanValue();
  }

  private TemplateManager recompilingTemplateManager;
  private StringWriter writer;
}
