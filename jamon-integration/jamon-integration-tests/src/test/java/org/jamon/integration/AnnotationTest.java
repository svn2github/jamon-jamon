/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.integration;

import junit.framework.TestCase;
import org.junit.Test;

import test.jamon.Annotated;
import test.jamon.AnnotatedImpl;

public class AnnotationTest extends TestCase {
  @Test
  public void testAnnotationOnProxy() {
    assertEquals("proxy", Annotated.class.getAnnotation(TestAnnotation.class).value());
  }

  @Test
  public void testAnnotationOnImpl() {
    assertNotNull("impl", AnnotatedImpl.class.getAnnotation(TestAnnotation.class).value());
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(AnnotationTest.class);
  }
}
