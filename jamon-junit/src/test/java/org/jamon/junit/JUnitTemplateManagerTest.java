/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.junit;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

import test.jamon.JUnitTemplate;

public class JUnitTemplateManagerTest extends TestCase {
  private JUnitTemplate template;
  private JUnitTemplateManager manager;
  private StringWriter writer;

  private void prepareTemplate(Integer iValue) throws Exception {
    HashMap<String, Object> optMap = new HashMap<String, Object>();
    if (iValue != null) {
      optMap.put("i", iValue);
    }
    manager = new JUnitTemplateManager(
      JUnitTemplate.class, optMap, new Object[] { Boolean.TRUE, "hello" });
    template = new JUnitTemplate(manager);
    writer = new StringWriter();
  }

  private void checkSuccess() throws Exception {
    assertTrue(manager.getWasRendered());
    assertEquals("", writer.toString());
  }

  public void testSuccess1() throws Exception {
    prepareTemplate(null);
    template.render(writer, true, "hello");
    checkSuccess();
  }

  public void testSuccess2() throws Exception {
    prepareTemplate(new Integer(4));
    template.setI(4).render(writer, true, "hello");
    checkSuccess();
  }

  public void testMissingOptionalArg() throws Exception {
    prepareTemplate(new Integer(4));
    try {
      template.render(writer, true, "hello");
      throw new Exception("all optional arguments not set not caught");
    }
    catch (AssertionFailedError e) {
      assertEquals("optional argument i not set", e.getMessage());
    }
  }

  public void testUnexpectedOptionalArg() throws Exception {
    prepareTemplate(null);
    try {
      template.setI(3).render(writer, true, "hello");
      throw new Exception("unexpected optional argument i not caught");
    }
    catch (AssertionFailedError e) {
      assertEquals("optional argument i set", e.getMessage());
    }
  }

  public void testMismatchRequiredArg() throws Exception {
    prepareTemplate(null);
    try {
      template.render(writer, false, "hello");
      throw new Exception("mismatch required argument b not caught");
    }
    catch (AssertionFailedError e) {
      assertEquals("required argument b expected:<true> but was:<false>", e.getMessage());
    }
  }

  public void testMismatchOptionalArg() throws Exception {
    prepareTemplate(new Integer(4));
    try {
      template.setI(3).render(writer, true, "hello");
      throw new Exception("mismatch optional argument i not caught");
    }
    catch (AssertionFailedError e) {
      assertEquals("optional argument i expected:<4> but was:<3>", e.getMessage());
    }
  }

  public void testConstructProxy() throws Exception {
    assertEquals(
      JUnitTemplate.class,
      new JUnitTemplateManager(
        JUnitTemplate.class,
        Collections.<String, Object> emptyMap(),
        new Object[0])
        .constructProxy("/test/jamon/JUnitTemplate").getClass());
  }
}
