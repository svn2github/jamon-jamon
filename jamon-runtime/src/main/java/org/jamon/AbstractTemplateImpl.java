/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

public abstract class AbstractTemplateImpl implements AbstractTemplateProxy.Intf {
  protected AbstractTemplateImpl(TemplateManager templateManager) {
    this.templateManager = templateManager;
  }

  protected AbstractTemplateImpl(TemplateManager templateManager,
    @SuppressWarnings("unused") AbstractTemplateProxy.ImplData implData) {
    this(templateManager);
  }

  protected TemplateManager getTemplateManager() {
    return templateManager;
  }

  private final TemplateManager templateManager;
}
