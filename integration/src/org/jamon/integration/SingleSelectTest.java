package org.jamon.integration;

import test.jamon.SelectCaller;

public class SingleSelectTest extends TestBase
{
   public void testRender() throws Exception
   {
      new SelectCaller().render(getWriter());
      assertEquals(
         "<select name=\"name\" >\n" +
         "    <option value=\"_1\" selected>1</option>\n" +
         "    <option value=\"_2\">2</option>\n" +
         "</select>",
         getOutput());
   }




}
