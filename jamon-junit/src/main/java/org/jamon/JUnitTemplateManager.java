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

package org.jamon;

import java.util.Map;

/**
 * A <code>TemplateManager</code> implementation suitable for use in constructing unit tests via
 * JUnit. A <code>JUnitTemplateManager<code> instance is not reusable, but
 * instead allows the "rendering" of the one template specified at
 * construction. For example, suppose the <code>/com/bar/FooTemplate</code> is declared as follows:
 *
 * <pre>
 *   &lt;%args&gt;
 *     int x;
 *     String s =&gt; "hello";
 *   &lt;/%args&gt;
 * </pre>
 *
 * To test that the method <code>showPage()</code> attempts to render the <code>FooTemplate</code>
 * with arguements <code>7</code> and <code>"bye"</code>, use something like the following code:
 *
 * <pre>
 * Map optArgs = new HashMap();
 * optArgs.put(&quot;s&quot;, &quot;bye&quot;);
 * JUnitTemplateManager jtm = new JUnitTemplateManager(&quot;/com/bar/FooTemplate&quot;, optArgs,
 *     new Object[] { new Integer(7) });
 *
 * TemplateManagerSource.setTemplateManager(jtm);
 * someObj.showPage();
 * assertTrue(jtm.getWasRendered());
 * </pre>
 *
 * @deprecated use {@link org.jamon.junit.JUnitTemplateManager}
 */

@Deprecated
public class JUnitTemplateManager extends org.jamon.junit.JUnitTemplateManager {
  /**
   * Construct a <code>JUnitTemplateManager</code>.
   *
   * @param path the template path
   * @param optionalArgs the expect optional arguments
   * @param requiredArgs the expected required argument values
   */
  public JUnitTemplateManager(
    String path, Map<String, Object> optionalArgs, Object[] requiredArgs) {
    super(path, optionalArgs, requiredArgs);
  }

  /**
   * Construct a <code>JUnitTemplateManager</code>.
   *
   * @param clazz the template class
   * @param optionalArgs the expect optional arguments
   * @param requiredArgs the expected required argument values
   */
  public JUnitTemplateManager(
    Class<? extends AbstractTemplateProxy> clazz,
    Map<String, Object> optionalArgs,
    Object[] requiredArgs) {
    super(clazz, optionalArgs, requiredArgs);
  }

}
