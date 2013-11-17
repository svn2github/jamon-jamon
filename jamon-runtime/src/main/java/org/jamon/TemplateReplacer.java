/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

import org.jamon.AbstractTemplateProxy.ImplDataCompatible;

/**
 * Possibly replaces proxy instances with proxy instances for replacement templates.
 */
public interface TemplateReplacer {
  /**
   * Get the replacement for a proxy. If the proxied template is not being replaced via a
   * &lt;%replaces&gt; tag in another template, simply return {@code proxy}. Otherwise, create the
   * appropriate replacement proxy instance and call
   * {@link ImplDataCompatible#populateFrom(org.jamon.AbstractTemplateProxy.ImplData)} on it's
   * implData instance.
   *
   * @param proxy the proxy to possibly replace.
   * @param jamonContext the jamonContext if there is one, or {@code null} otherwise.
   * @return {@code p_proxy} or a proxy instance for a template replacing p_proxy's template.
   */
  AbstractTemplateProxy getReplacement(AbstractTemplateProxy proxy, Object jamonContext);
}
