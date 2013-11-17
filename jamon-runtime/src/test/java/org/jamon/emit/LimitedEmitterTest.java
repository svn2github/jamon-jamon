/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.emit;

import org.junit.Test;
import static org.junit.Assert.*;

public class LimitedEmitterTest {
  @Test
  public void testPrimativeEmits() {
    assertEquals("true", LimitedEmitter.valueOf(true));
    assertEquals("false", LimitedEmitter.valueOf(false));
    assertEquals("x", LimitedEmitter.valueOf('x'));
    assertEquals("0", LimitedEmitter.valueOf((byte) 0));
    assertEquals("0", LimitedEmitter.valueOf((short) 0));
    assertEquals("0", LimitedEmitter.valueOf(0));
    assertEquals("0", LimitedEmitter.valueOf(0L));
    assertEquals("0.0", LimitedEmitter.valueOf(0.0f));
    assertEquals("0.0", LimitedEmitter.valueOf(0.0d));
  }

  @Test
  public void testWrapperEmits() {
    assertEquals("true", LimitedEmitter.valueOf(Boolean.TRUE));
    assertEquals("false", LimitedEmitter.valueOf(Boolean.FALSE));
    assertEquals("x", LimitedEmitter.valueOf(Character.valueOf('x')));
    assertEquals("0", LimitedEmitter.valueOf(Byte.valueOf((byte) 0)));
    assertEquals("0", LimitedEmitter.valueOf(Short.valueOf((short) 0)));
    assertEquals("0", LimitedEmitter.valueOf(Integer.valueOf(0)));
    assertEquals("0", LimitedEmitter.valueOf(Long.valueOf(0L)));
    assertEquals("0.0", LimitedEmitter.valueOf(Float.valueOf(0.0f)));
    assertEquals("0.0", LimitedEmitter.valueOf(Double.valueOf(0.0d)));
  }

  @Test
  public void testNullWrapperEmits() {
    assertEquals("", LimitedEmitter.valueOf((Boolean) null));
    assertEquals("", LimitedEmitter.valueOf((Byte) null));
    assertEquals("", LimitedEmitter.valueOf((Short) null));
    assertEquals("", LimitedEmitter.valueOf((Character) null));
    assertEquals("", LimitedEmitter.valueOf((Integer) null));
    assertEquals("", LimitedEmitter.valueOf((Long) null));
    assertEquals("", LimitedEmitter.valueOf((Float) null));
    assertEquals("", LimitedEmitter.valueOf((Double) null));
  }

  @Test
  public void testStringEmits() {
    assertEquals("", LimitedEmitter.valueOf((String) null));
    assertEquals("", LimitedEmitter.valueOf(""));
    assertEquals("hello", LimitedEmitter.valueOf("hello"));
  }
}
