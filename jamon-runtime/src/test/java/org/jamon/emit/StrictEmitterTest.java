/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.emit;

import static org.junit.Assert.*;

import org.junit.Test;

public class StrictEmitterTest {

  @Test
  public void testString() {
    assertEquals("hello", StrictEmitter.valueOf("hello"));
  }

  @Test
  public void testNull() {
    assertEquals("", StrictEmitter.valueOf(null));
  }

}
