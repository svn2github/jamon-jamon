/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

/**
 * A <code>TemplateManager</code> is the entry point to obtaining instances of template objects.
 *
 * @see TemplateManagerSource
 */
public interface TemplateManager {
  /**
   * Given a proxy, return an instance of the executable code for that proxy's template or a
   * suitable replacement.
   *
   * @param proxy a proxy for the template
   * @return a <code>Template</code> instance
   **/
  AbstractTemplateProxy.Intf constructImpl(AbstractTemplateProxy proxy);

  /**
   * Given a proxy and a jamonContext, return an instance of the executable code for that proxy's
   * template or a suitable replacement, possibly based on the jamonContext.
   *
   * @param proxy a proxy for the template
   * @param jamonContext the current jamonContext (can be {@code null})
   * @return a <code>Template</code> instance
   **/
  AbstractTemplateProxy.Intf constructImpl(AbstractTemplateProxy proxy, Object jamonContext);

  /**
   * Given a template path, return a proxy for that template.
   *
   * @param path the path to the template
   * @return a <code>Template</code> instance
   */
  AbstractTemplateProxy constructProxy(String path);
}
