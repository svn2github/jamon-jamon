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
 * Contributor(s): Ian Robertson
 */

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
