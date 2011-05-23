package org.jamon.compatability;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;


public class CompatabilityTest {
  @Deprecated
  @Test
  public void testCompatability() throws Exception {
    StringWriter stringWriter = new StringWriter();
    new IteratorCall().render(stringWriter);
    assertEquals("(a)(b)\ntest", stringWriter.toString());
  }
}
