package org.jamon;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

public class AbstractRendererTest {

  private static class HelloRenderer extends AbstractRenderer {
    @Override
    public void renderTo(Writer writer) throws IOException {
      writer.write("Hello");
    }
  }

  @Test
  public void testAsString() {
    assertEquals("Hello", new HelloRenderer().asString());
  }

}
