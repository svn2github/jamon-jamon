/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

import org.jamon.AbstractTemplateProxy.Intf;

/**
 * An abstract implementation of {@code TemplateManager} which handles some of the common tasks
 * surrounding template replacement.
 */
public abstract class AbstractTemplateManager implements TemplateManager {
  private final TemplateReplacer templateReplacer;

  protected AbstractTemplateManager() {
    templateReplacer = IdentityTemplateReplacer.INSTANCE;
  }

  protected AbstractTemplateManager(TemplateReplacer templateReplacer) {
    this.templateReplacer = templateReplacer;
  }

  protected TemplateReplacer getTemplateReplacer() {
    return templateReplacer;
  }

  /**
   * {@inheritDoc} This implementation simply calls
   * {@link #constructImpl(AbstractTemplateProxy, Object) constructImpl(p_proxy, null)}.
   **/
  @Override
  public Intf constructImpl(AbstractTemplateProxy proxy) {
    return constructImpl(proxy, null);
  }

  /**
   * {@inheritDoc} This implementation simply calls <code>
   * <pre>
   *   constructImplFromReplacedProxy(
   *     getTemplateReplacer().getReplacement(p_proxy, p_jamonContext))
     * </pre>
   **/
  @Override
  public Intf constructImpl(AbstractTemplateProxy proxy, Object jamonContext) {
    return constructImplFromReplacedProxy(
      getTemplateReplacer().getReplacement(proxy, jamonContext));
  }

  protected abstract Intf constructImplFromReplacedProxy(AbstractTemplateProxy replacedProxy);
}
