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

package org.jamon.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.jamon.api.TemplateLocation;
import org.jamon.api.TemplateSource;

/**
 * An implementation of {@link TemplateSource} which retrieves templates from Java resources.
 */
public class ResourceTemplateSource implements TemplateSource {
  public ResourceTemplateSource(ClassLoader classLoader, String templateSourceDir) {
    this(classLoader, templateSourceDir, "jamon");
  }

  /**
   * Construct a ResourceTemplateSource, specifying a filename extension for templates. If the
   * supplied extension is null or empty, no extension is expected, otherwise the extension should
   * <emph>NOT</emph> include a leading ".".
   *
   * @param classLoader the classloader to use to load resources
   * @param templateSourceDir the source directory
   * @param extension the filename extension for templates
   */
  public ResourceTemplateSource(
    ClassLoader classLoader, String templateSourceDir, String extension) {
    this.classLoader = classLoader;
    this.templateSourceDir = templateSourceDir;
    this.extension = extension == null || extension.length() == 0
        ? ""
        : "." + extension;
  }

  @Override
  public long lastModified(String templatePath) throws IOException {
    return getTemplate(templatePath).getLastModified();
  }

  @Override
  public boolean available(String templatePath) {
    // FIXME: is this the way to implement this?
    return getUrl(templatePath) != null;
  }

  @Override
  public InputStream getStreamFor(String templatePath) throws IOException {
    return getTemplate(templatePath).getInputStream();
  }

  @Override
  public String getExternalIdentifier(String templatePath) {
    // return getUrl(templatePath).toExternalForm();
    return templatePath;
  }

  private URL getUrl(String templatePath) {
    return classLoader.getResource(resourceName(templatePath));
  }

  private String resourceName(String templatePath) {
    return templateSourceDir + templatePath + extension;
  }

  private URLConnection getTemplate(String templatePath) throws IOException {
    return getUrl(templatePath).openConnection();
  }

  private final ClassLoader classLoader;
  private final String templateSourceDir;
  private final String extension;

  @Override
  public TemplateLocation getTemplateLocation(String templatePath) {
    return new TemplateResourceLocation(resourceName(templatePath));
  }

  @Override
  public void loadProperties(String dirPath, Properties properties) throws IOException {
    InputStream inputStream =
      classLoader.getResourceAsStream(templateSourceDir + dirPath + "/jamon.properties");
    if (inputStream != null) {
      properties.load(inputStream);
    }
  }
}
