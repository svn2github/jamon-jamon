/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
