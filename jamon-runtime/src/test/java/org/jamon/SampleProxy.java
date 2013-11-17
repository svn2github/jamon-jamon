/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon;

public class SampleProxy extends AbstractTemplateProxy {
  public final class SampleImpl extends AbstractTemplateImpl {
    private SampleImpl(TemplateManager manager) {
      super(manager);
    }
  }

  public SampleProxy(String path) {
    super(path);
  }

  public SampleProxy(TemplateManager templateManager) {
    super(templateManager);
  }

  @Override
  public AbstractTemplateImpl constructImpl(Class<? extends AbstractTemplateImpl> p_class) {
    return null;
  }

  @Override
  protected AbstractTemplateImpl constructImpl() {
    return new SampleImpl(getTemplateManager());
  }

  @Override
  protected ImplData makeImplData() {
    return null;
  }
}
