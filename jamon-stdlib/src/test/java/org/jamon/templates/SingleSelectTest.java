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
