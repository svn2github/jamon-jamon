package org.jamon.emit;

import java.util.Date;

import org.junit.Test;
import static org.junit.Assert.*;

public class StandardEmitterTest {
  @Test public void testPrimativeEmits() {
    assertEquals("true", StandardEmitter.valueOf(true));
    assertEquals("false", StandardEmitter.valueOf(false));
    assertEquals("x", StandardEmitter.valueOf('x'));
    assertEquals("0", StandardEmitter.valueOf((short) 0));
    assertEquals("0", StandardEmitter.valueOf(0));
    assertEquals("0", StandardEmitter.valueOf(0L));
    assertEquals("0.0", StandardEmitter.valueOf(0.0f));
    assertEquals("0.0", StandardEmitter.valueOf(0.0d));
  }

  @Test public void testWrapperEmits() {
    assertEquals("true", StandardEmitter.valueOf(Boolean.TRUE));
    assertEquals("false", StandardEmitter.valueOf(Boolean.FALSE));
    assertEquals("x", StandardEmitter.valueOf(Character.valueOf('x')));
    assertEquals("0", StandardEmitter.valueOf(Short.valueOf((short) 0)));
    assertEquals("0", StandardEmitter.valueOf(Integer.valueOf(0)));
    assertEquals("0", StandardEmitter.valueOf(Long.valueOf(0L)));
    assertEquals("0.0", StandardEmitter.valueOf(Float.valueOf(0.0f)));
    assertEquals("0.0", StandardEmitter.valueOf(Double.valueOf(0.0d)));
  }

  @Test public void testNullWrapperEmits() {
    assertEquals("", StandardEmitter.valueOf((Boolean) null));
    assertEquals("", StandardEmitter.valueOf((Short) null));
    assertEquals("", StandardEmitter.valueOf((Character) null));
    assertEquals("", StandardEmitter.valueOf((Integer) null));
    assertEquals("", StandardEmitter.valueOf((Long) null));
    assertEquals("", StandardEmitter.valueOf((Float) null));
    assertEquals("", StandardEmitter.valueOf((Double) null));
  }

  @Test public void testStringEmits() {
    assertEquals("", StandardEmitter.valueOf((String) null));
    assertEquals("", StandardEmitter.valueOf(""));
    assertEquals("hello", StandardEmitter.valueOf("hello"));
  }

  @Test public void testObjectEmits() {
    Date d = new Date();
    assertEquals(d.toString(), StandardEmitter.valueOf(d));
    assertEquals("", StandardEmitter.valueOf((Date) null));
  }
}
