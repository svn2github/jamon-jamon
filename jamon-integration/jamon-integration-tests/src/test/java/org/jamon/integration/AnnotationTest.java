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
