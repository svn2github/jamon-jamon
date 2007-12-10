package org.jamon.emit;

import static org.junit.Assert.*;

import org.junit.Test;

public class StrictEmitterTest {

  @Test public void testString() {
    assertEquals("hello", StrictEmitter.valueOf("hello"));
  }

  @Test public void testNull() {
    assertEquals("", StrictEmitter.valueOf(null));
  }

}
