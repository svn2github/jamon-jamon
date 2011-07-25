package org.jamon;

import static org.junit.Assert.*;

import org.junit.Test;

public class BasicTemplateManagerTest {
  @Test
  public void testNoArgConstructor() {
    assertEquals(
      SampleProxy.class,
      new BasicTemplateManager().constructProxy("/org/jamon/SampleProxy").getClass());
  }

  @Test
  public void testBasicTemplateManagerClassLoader() {
    assertEquals(
      SampleProxy.class,
      new BasicTemplateManager(SampleProxy.class.getClassLoader())
        .constructProxy("/org/jamon/SampleProxy").getClass());
  }

  @Test
  public void testConstructImpl() {
    assertEquals(
      SampleProxy.SampleImpl.class,
      new BasicTemplateManager().constructImpl(new SampleProxy("x")).getClass());
  }
}
