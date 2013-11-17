/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.templates;

import org.jamon.stdlib.tests.SelectCaller;

@Deprecated public class SingleSelectTest extends TestBase
{
   public void testRender() throws Exception
   {
      new SelectCaller().render(getWriter());
      checkOutput(
         "<select name=\"name\" >\n" +
         "    <option value=\"_1\" selected>1</option>\n" +
         "    <option value=\"_2\">2</option>\n" +
         "</select>");
   }




}
