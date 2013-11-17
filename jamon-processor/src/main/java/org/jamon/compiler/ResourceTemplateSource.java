/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
