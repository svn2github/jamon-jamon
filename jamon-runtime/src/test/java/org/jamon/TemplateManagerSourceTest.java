/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

import org.jamon.AbstractTemplateProxy;
import org.jamon.AbstractTemplateProxy.Intf;
import org.jamon.TemplateManager;
import org.jamon.TemplateManagerSource;

import junit.framework.TestCase;

public class TemplateManagerSourceTest extends TestCase {
  public void testSetSource() {
    final TemplateManager tm = new TestTemplateManager();
    TemplateManagerSource.setTemplateManagerSource(new TemplateManagerSource() {
      @Override
      public TemplateManager getTemplateManagerForPath(String path) {
        return tm;
      }
    });
    assertSame(tm, TemplateManagerSource.getTemplateManagerFor(""));
  }

  public void testSetManager() {
    TemplateManager tm = new TestTemplateManager();
    TemplateManagerSource.setTemplateManager(tm);
    assertSame(tm, TemplateManagerSource.getTemplateManagerFor(""));
  }

  private static class TestTemplateManager extends AbstractTemplateManager {

    @Override
    public AbstractTemplateProxy constructProxy(String path) {
      return null;
    }

    @Override
    protected Intf constructImplFromReplacedProxy(AbstractTemplateProxy replacedProxy) {
      return null;
    }
  }
}
