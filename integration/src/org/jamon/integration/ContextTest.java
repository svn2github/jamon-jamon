package org.jamon.integration;

import test.jamon.ContextCaller;

public class ContextTest extends TestBase
{
    public void testContext() throws Exception
    {
        new ContextCaller()
            .setJamonContext(new TestJamonContext(3))
            .render(getWriter());
        checkOutput("Caller: 3\n" +
                    "Parent: 3\nCallee: 3\nCalleeFragment: 3\n" +
                    "Def: 3\n");
    }
}
