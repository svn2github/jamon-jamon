/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.codegen;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jamon.api.TemplateLocation;
import org.jamon.api.TemplateSource;
import org.jamon.emit.EmitMode;

import junit.framework.TestCase;

public class TemplateDescriberTest extends TestCase {
  private MockTemplateSource mockTemplateSource;

  private TemplateDescriber templateDescriber;

  private static class MockTemplateSource implements TemplateSource {
    @Override
    public long lastModified(String templatePath) {
      return 0;
    }

    @Override
    public boolean available(String templatePath) {
      return false;
    }

    @Override
    public InputStream getStreamFor(String templatePath) {
      return null;
    }

    @Override
    public String getExternalIdentifier(String templatePath) {
      return null;
    }

    @Override
    public TemplateLocation getTemplateLocation(String templatePath) {
      return null;
    }

    public void setProperties(String path, Properties properties) {
      this.properties.put(path, properties);
    }

    @Override
    public void loadProperties(String path, Properties properties) {
      Properties pathProperties = this.properties.get(path);
      if (pathProperties != null) {
        properties.putAll(pathProperties);
      }
    }

    private Map<String, Properties> properties = new HashMap<String, Properties>();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mockTemplateSource = new MockTemplateSource();
    templateDescriber = new TemplateDescriber(mockTemplateSource, null);
  }

  public void testGetAliases() throws Exception {
    assertEquals(0, templateDescriber.getAliases("/foo").size());
    Properties properties = new Properties();
    properties.put("org.jamon.alias.bar", "/a/b");
    mockTemplateSource.setProperties("/", properties);
    assertEquals(1, templateDescriber.getAliases("/foo").size());
    assertEquals("/a/b", templateDescriber.getAliases("/foo").get("bar"));
    Properties subProperties = new Properties();
    subProperties.put("org.jamon.alias.bar", "/b/c");
    mockTemplateSource.setProperties("/foo/", subProperties);
    subProperties.put("org.jamon.alias.bar2", "/c/d");
    assertEquals("/b/c", templateDescriber.getAliases("/foo/a").get("bar"));
    assertEquals("/c/d", templateDescriber.getAliases("/foo/a").get("bar2"));
    subProperties.put("org.jamon.alias.bar", "");
    assertFalse(templateDescriber.getAliases("/foo/a").containsKey("bar"));
  }

  public void testGetJamonContextType() throws Exception {
    assertNull(templateDescriber.getJamonContextType("/foo"));

    Properties properties = new Properties();
    properties.put("org.jamon.contextType", "foo.bar");
    mockTemplateSource.setProperties("/", properties);
    assertEquals("foo.bar", templateDescriber.getJamonContextType("/foo"));

    Properties subProperties = new Properties();
    subProperties.put("org.jamon.contextType", "foo.baz");
    mockTemplateSource.setProperties("/foo/", subProperties);
    assertEquals("foo.baz", templateDescriber.getJamonContextType("/foo/bat"));

    Properties subSubProperties = new Properties();
    subProperties.put("org.jamon.contextType", "");
    mockTemplateSource.setProperties("/foo/bar/", subSubProperties);
    assertNull(templateDescriber.getJamonContextType("/foo/bar/baz"));
  }

  public void testGetEmitMode() throws Exception {
    Properties properties = new Properties();
    setUp();
    assertEquals(EmitMode.STANDARD, templateDescriber.getEmitMode("/foo"));
    properties.put("org.jamon.emitMode", "strict");
    mockTemplateSource.setProperties("/", properties);
    assertEquals(EmitMode.STRICT, templateDescriber.getEmitMode("/foo"));
  }

  public void testGetEscaping() throws Exception {
    assertNull(templateDescriber.getEscaping("/foo/bar"));
    Properties properties = new Properties();
    properties.put("org.jamon.escape", "H");
    mockTemplateSource.setProperties("/", properties);
    assertEquals(EscapingDirective.get("H"), templateDescriber.getEscaping("/foo/bar"));
    Properties subProperties = new Properties();
    subProperties.put("org.jamon.escape", "x");
    mockTemplateSource.setProperties("/foo/", subProperties);
    assertEquals(EscapingDirective.get("x"), templateDescriber.getEscaping("/foo/bar"));
  }
}
