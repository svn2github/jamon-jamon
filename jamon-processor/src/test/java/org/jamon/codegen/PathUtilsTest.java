/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.codegen;

import static org.junit.Assert.*;

import org.junit.Test;

public class PathUtilsTest {

  @Test
  public void testGetPathForProxyClass() {
    assertEquals("org/jamon/codegen/PathUtilsTest", PathUtils.getPathForProxyClass(getClass()));
  }

}
