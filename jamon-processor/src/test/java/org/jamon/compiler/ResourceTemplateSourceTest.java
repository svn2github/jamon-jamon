/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.compiler;

import junit.framework.TestCase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.net.URL;

import org.jamon.compiler.ResourceTemplateSource;

public class ResourceTemplateSourceTest extends TestCase {
  private final String PREFIX = "foo";
  private File tmpDir;
  private File subDir;

  @Override
  protected void setUp() throws Exception {
    tmpDir = File.createTempFile("jrt", "");
    tmpDir.delete();
    tmpDir.mkdirs();
    subDir = new File(tmpDir, PREFIX);
    subDir.mkdirs();
  }

  @Override
  protected void tearDown() throws Exception {
    fullDelete(tmpDir);
  }

  private static void fullDelete(File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; ++i) {
        if (files[i].isDirectory()) {
          fullDelete(files[i]);
        }
        else {
          files[i].delete();
        }
      }
    }
    directory.delete();
  }

  public void testResources() throws Exception {
    ResourceTemplateSource source = new ResourceTemplateSource(
      new URLClassLoader(new URL[] { tmpDir.toURI().toURL() }),
      PREFIX);
    final String pkg = "pkg";
    File pkgDir = new File(subDir, pkg);
    pkgDir.mkdirs();
    File f = File.createTempFile("abcd", ".jamon", pkgDir);
    FileOutputStream fos = new FileOutputStream(f);
    final byte[] contents = "This is a test.\n".getBytes("US-ASCII");
    fos.write(contents);
    fos.close();
    final long now = System.currentTimeMillis() / 1000l;
    String tName = f.getName();
    int j = tName.indexOf(".");
    tName = tName.substring(0, j);
    String tmpl = "/" + pkg + "/" + tName;
    assertTrue(source.available(tmpl));
    assertFalse(source.available(tmpl + "x"));
    assertTrue(Math.abs(now - source.lastModified(tmpl) / 1000l) <= 1);
    byte[] read = new byte[contents.length];
    InputStream is = null;
    try {
      is = source.getStreamFor(tmpl);
      assertEquals(read.length, is.read(read));
      assertEquals(-1, is.read());
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
    for (int i = 0; i < read.length; ++i) {
      assertEquals(contents[i], read[i]);
    }
  }
}
