/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

/**
 * The source for obtaining a default {@link TemplateManager}. This class is used to find a
 * <code>TemplateManager</code> when a template is instantiated without supplying a TemplateManager,
 * e.g.
 *
 * <pre>
 * MyFooTemplate template = new MyFooTemplate();
 * </pre>
 *
 * Note that this is an abstract class instead of an interface in order to allow static methods.
 */

public abstract class TemplateManagerSource {
  /**
   * Get a {@link TemplateManager} for a specified template path.
   *
   * @param path the template path
   * @return a TemplateManager appropriate for that path
   */
  public abstract TemplateManager getTemplateManagerForPath(String path);

  /**
   * Get the TemplateManager for the specified path.
   *
   * @param path the template path
   * @return a TemplateManager appropriate for that path
   */
  public static TemplateManager getTemplateManagerFor(String path) {
    return getTemplateManagerSource().getTemplateManagerForPath(path);
  }

  /**
   * Set the TemplateManagerSource.
   *
   * @param source the TemplateManagerSource
   */
  public static void setTemplateManagerSource(TemplateManagerSource source) {
    TemplateManagerSource.source = source;
  }

  /**
   * Set the TemplateManagerSource by supplying a single TemplateManager which will be supplied by
   * {@link #getTemplateManagerFor} for all paths.
   *
   * @param manager the TemplateManager
   */
  public static void setTemplateManager(final TemplateManager manager) {
    setTemplateManagerSource(new TemplateManagerSource() {
      @Override
      public TemplateManager getTemplateManagerForPath(String path) {
        return manager;
      }
    });
  }

  private static TemplateManagerSource getTemplateManagerSource() {
    return source;
  }

  private static TemplateManagerSource source;

  static {
    setTemplateManager(new BasicTemplateManager());
  }
}
