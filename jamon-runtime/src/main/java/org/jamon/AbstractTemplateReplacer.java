/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

import org.jamon.AbstractTemplateProxy.ImplData;
import org.jamon.AbstractTemplateProxy.ImplDataCompatible;
import org.jamon.AbstractTemplateProxy.ReplacementConstructor;

/**
 * A base class for classes wishing to define a {@code TemplateReplacer}. Implementors need only
 * define {@link #findReplacement(Class, Object)}
 */
public abstract class AbstractTemplateReplacer implements TemplateReplacer {
  @Override
  public AbstractTemplateProxy getReplacement(AbstractTemplateProxy proxy, Object jamonContext) {
    ReplacementConstructor constructor = findReplacement(proxy.getClass(), jamonContext);
    if (constructor != null) {
      AbstractTemplateProxy replacedProxy = constructor.makeReplacement();
      @SuppressWarnings("unchecked")
      ImplDataCompatible<ImplData> replacedImplData =
        (ImplDataCompatible<ImplData>) replacedProxy.getImplData();
      replacedImplData.populateFrom(proxy.getImplData());
      return replacedProxy;
    }
    else {
      return proxy;
    }
  }

  /**
   * Find an appropriate {@link ReplacementConstructor} for a template, if there is one.
   *
   * @param proxyClass the class to find a replacement for.
   * @param jamonContext the jamonContext
   * @return the {@code ReplacementConstructor} for the template which will serve as a replacement,
   *         or {@code null} if there is to be no replacement performed.
   */
  protected abstract ReplacementConstructor findReplacement(
    Class<? extends AbstractTemplateProxy> proxyClass, Object jamonContext);

}
