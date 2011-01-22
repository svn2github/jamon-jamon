package org.jamon.integration;

import test.jamon.Import;

public class ImportTest extends TestBase
{
    public void testExercise() throws Exception
    {
        new Import().render(getWriter());
        checkOutput("1");
    }
}
