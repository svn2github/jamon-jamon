package org.jamon.codegen;

import static org.junit.Assert.*;

import org.junit.Test;

public class PathUtilsTest {

  @Test
  public void testGetPathForProxyClass() {
    assertEquals("org/jamon/codegen/PathUtilsTest", PathUtils.getPathForProxyClass(getClass()));
  }

}
