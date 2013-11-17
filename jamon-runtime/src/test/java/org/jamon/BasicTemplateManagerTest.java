/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon;

import static org.junit.Assert.*;

import org.junit.Test;

public class BasicTemplateManagerTest {
  @Test
  public void testNoArgConstructor() {
    assertEquals(
      SampleProxy.class,
      new BasicTemplateManager().constructProxy("/org/jamon/SampleProxy").getClass());
  }

  @Test
  public void testBasicTemplateManagerClassLoader() {
    assertEquals(
      SampleProxy.class,
      new BasicTemplateManager(SampleProxy.class.getClassLoader())
        .constructProxy("/org/jamon/SampleProxy").getClass());
  }

  @Test
  public void testConstructImpl() {
    assertEquals(
      SampleProxy.SampleImpl.class,
      new BasicTemplateManager().constructImpl(new SampleProxy("x")).getClass());
  }
}
