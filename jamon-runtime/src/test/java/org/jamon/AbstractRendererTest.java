/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
