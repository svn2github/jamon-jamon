/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {
  public void testPathToClassName() throws Exception {
    assertEquals("a.b.c", StringUtils.templatePathToClassName("/a/b/c"));
    assertEquals("a.b.c", StringUtils.templatePathToClassName("a/b/c"));
    assertEquals("a", StringUtils.templatePathToClassName("a"));
    assertEquals("a", StringUtils.templatePathToClassName("/a"));
    assertEquals("axy.bxy.cxyasd", StringUtils.templatePathToClassName("/axy/bxy/cxyasd"));
  }

  public void testClassNameToPath() throws Exception {

    assertEquals(File.separator + "a" + File.separator + "b" + File.separator + "c", StringUtils
        .classNameToFilePath("a.b.c"));
    assertEquals(File.separator + "a", StringUtils.classNameToFilePath("a"));
    assertEquals(
      File.separator + "axy" + File.separator + "bxy" + File.separator + "cxyasd",
      StringUtils.classNameToFilePath("axy.bxy.cxyasd"));
  }

  public void testCapitalize() throws Exception {
    assertEquals("", StringUtils.capitalize(""));
    assertEquals("A", StringUtils.capitalize("a"));
    assertEquals("G", StringUtils.capitalize("g"));
    assertEquals("G", StringUtils.capitalize("G"));
    assertEquals("!", StringUtils.capitalize("!"));
    assertEquals("Abcde", StringUtils.capitalize("Abcde"));
    assertEquals("ABCDE", StringUtils.capitalize("ABCDE"));
    assertEquals("Abcde", StringUtils.capitalize("abcde"));
    assertEquals("AbCdE", StringUtils.capitalize("abCdE"));
    assertEquals("1234abcde", StringUtils.capitalize("1234abcde"));
    assertEquals(null, StringUtils.capitalize(null));
  }

  public void testHexify() throws Exception {
    assertEquals("0045", StringUtils.hexify4(0x45));
    assertEquals("fe32", StringUtils.hexify4(0xFE32));
    assertEquals("0003", StringUtils.hexify4(3));
    assertEquals("0000", StringUtils.hexify4(0));
    assertEquals("ffff", StringUtils.hexify4(0xffff));
    assertEquals("03b8", StringUtils.hexify4(0x03b8));
  }

  public void testCommaJoin0() throws Exception {
    StringBuilder buf = new StringBuilder("prefix ");
    StringUtils.commaJoin(buf, Collections.<String> emptyList());
    assertEquals("prefix ", buf.toString());
  }

  public void testCommaJoin1() throws Exception {
    StringBuilder buf = new StringBuilder("prefix ");
    StringUtils.commaJoin(buf, Arrays.asList("foo"));
    assertEquals("prefix foo", buf.toString());
  }

  public void testCommaJoin2() throws Exception {
    StringBuilder buf = new StringBuilder("prefix ");
    StringUtils.commaJoin(buf, (Arrays.asList("foo", "bar")));
    assertEquals("prefix foo, bar", buf.toString());
  }

  public void testCommaJoin3() throws Exception {
    StringBuilder buf = new StringBuilder("prefix ");
    StringUtils.commaJoin(buf, Arrays.asList("foo", "bar", "baz"));
    assertEquals("prefix foo, bar, baz", buf.toString());
  }

  public void testTemplatePathToFileDir() throws Exception {
    assertEquals("", StringUtils.templatePathToFileDir("abcd"));
    assertEquals("", StringUtils.templatePathToFileDir("/abcd"));
    assertEquals(File.separator + "a", StringUtils.templatePathToFileDir("/a/bcd"));
    assertEquals(File.separator + "ab", StringUtils.templatePathToFileDir("/ab/cd"));
    assertEquals(
      File.separator + "a" + File.separator + "b", StringUtils.templatePathToFileDir("/a/b/cd"));
    assertEquals(
      File.separator + "a" + File.separator + "b" + File.separator + "c",
      StringUtils.templatePathToFileDir("/a/b/c/d"));
  }

  public void testIsGeneratedClassFilename() throws Exception {
    assertTrue(StringUtils.isGeneratedClassFilename("abc", "abc.class"));
    assertTrue(StringUtils.isGeneratedClassFilename("abc", "abc$1.class"));
    assertTrue(StringUtils.isGeneratedClassFilename("abc", "abc$asdf$1.class"));
    assertTrue(!StringUtils.isGeneratedClassFilename("abc", "abc.java"));
    assertTrue(!StringUtils.isGeneratedClassFilename("abc", "ab.class"));
    assertTrue(!StringUtils.isGeneratedClassFilename("abc", "ab1.class"));
    assertTrue(!StringUtils.isGeneratedClassFilename("abc", "abc1.class"));
    assertTrue(!StringUtils.isGeneratedClassFilename("abc", "abc1$123.class"));
    assertTrue(!StringUtils.isGeneratedClassFilename("abc", "abc1$123$4.class"));
  }

  public void testFilePathToTemplatePath() throws Exception {
    assertEquals(
      "/a/b/c.jamon",
      StringUtils.filePathToTemplatePath(
        File.separator + "a" + File.separator + "b" + File.separator + "c.jamon"));
  }
}
