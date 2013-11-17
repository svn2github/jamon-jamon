/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
