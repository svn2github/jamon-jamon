/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * An interface representing a "repository" of template sources. One obvious implementation would be
 * a file-system based implementation (e.g. {@link org.jamon.compiler.FileTemplateSource}), but
 * others might include storing templates in a database, or retrieving them remotely via HTTP.
 */

public interface TemplateSource {
  /**
   * Determines when the indicated template was last modified, in ms since the epoch.
   * 
   * @param templatePath the path to the template
   * @return the timestamp of when the template was last modified
   */
  long lastModified(String templatePath) throws IOException;

  /**
   * Determines whether the indicated template source is available.
   * 
   * @param templatePath the path to the template
   * @return whether the template source is available
   */
  boolean available(String templatePath) throws IOException;

  /**
   * Get a {@link InputStream} for the source of the specified template.
   * 
   * @param templatePath the path to the template
   * @return an InputStream for the data comprising the template
   */
  InputStream getStreamFor(String templatePath) throws IOException;

  /**
   * Get an identifying string for the specified template.
   * 
   * @param templatePath the path to the template
   * @return an identifying string
   */
  String getExternalIdentifier(String templatePath);

  TemplateLocation getTemplateLocation(String templatePath);

  /**
   * Load any properties that might be used to influence the processing of templates within the
   * specified directory.
   * 
   * @param path The directory to to look for
   * @param properties The {@code Properties} instance to add any found properties to.
   * @throws IOException
   */
  void loadProperties(String path, Properties properties) throws IOException;
}
