/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

public abstract class AbstractTemplateProxy {
  public interface Intf {}

  /**
   * A constructor for a proxy class for a replacing template. This is used to avoid reflection
   * after startup.
   */
  public interface ReplacementConstructor {
    AbstractTemplateProxy makeReplacement();
  }

  public static class ImplData {}

  public static interface ImplDataCompatible<T extends ImplData> {
    public void populateFrom(T implData);
  }

  protected AbstractTemplateProxy(TemplateManager templateManager) {
    this.templateManager = templateManager;
  }

  protected AbstractTemplateProxy(String path) {
    this(TemplateManagerSource.getTemplateManagerFor(path));
  }

  protected final TemplateManager getTemplateManager() {
    return templateManager;
  }

  private final TemplateManager templateManager;

  private ImplData implData = makeImplData();

  public abstract AbstractTemplateImpl constructImpl(Class<? extends AbstractTemplateImpl> clazz);

  protected abstract AbstractTemplateImpl constructImpl();

  protected abstract ImplData makeImplData();

  protected final void reset() {
    implData = null;
  }

  public ImplData getImplData() {
    if (implData == null) {
      throw new IllegalStateException("Template has been used");
    }
    return implData;
  }
}
