/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.util;

import static org.junit.Assert.*;

import org.jamon.annotations.Argument;
import org.jamon.annotations.Fragment;
import org.jamon.annotations.Template;
import org.junit.Test;

public class AnnotationReflectorTest {

  @Template(
    signature = "123",
    inheritanceDepth = 5,
    abstractMethodNames = { "a", "b" },
    requiredArguments = { @Argument(name = "i", type = "int") },
    fragmentArguments = {
        @Fragment(name = "f", requiredArguments = { @Argument(name = "x", type = "float") }) })
  private static class SampleTemplate {}

  @Test
  public void testReflection() {
    AnnotationReflector reflector = new AnnotationReflector(SampleTemplate.class);
    Template templateAnnotation = reflector.getAnnotation(Template.class);
    assertEquals("123", templateAnnotation.signature());
    assertEquals(5, templateAnnotation.inheritanceDepth());
    assertArrayEquals(new String[] { "a", "b" }, templateAnnotation.abstractMethodNames());

    Argument[] requiredArguments = templateAnnotation.requiredArguments();
    assertEquals(1, requiredArguments.length);
    assertEquals("int", requiredArguments[0].type());

    Fragment[] fragments = templateAnnotation.fragmentArguments();
    assertEquals(1, fragments.length);
    assertEquals("f", fragments[0].name());
    Argument[] fragArgs = fragments[0].requiredArguments();
    assertEquals(1, fragArgs.length);
    assertEquals("float", fragArgs[0].type());
  }
}
