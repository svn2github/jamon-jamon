/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

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
