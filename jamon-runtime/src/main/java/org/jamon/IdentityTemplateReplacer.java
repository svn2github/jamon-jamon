/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

/**
 * A {@code TemplateReplacer} which always returns the original proxy.
 */
public enum IdentityTemplateReplacer implements TemplateReplacer {
  INSTANCE;

  /**
   * Simply return {@code proxy}.
   *
   * @param proxy the proxy which will not be replaced
   * @param jamonContext the jamonContext (ignored)
   * @return {@code proxy}
   */
  @Override
  public AbstractTemplateProxy getReplacement(AbstractTemplateProxy proxy, Object jamonContext) {
    return proxy;
  }
}
