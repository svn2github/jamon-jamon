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
