package org.jamon.integration;

import test.jamon.For;
import test.jamon.While;

public class ControlFlowTest extends TestBase
{
    public void testWhile() throws Exception
    {
        new While().render(getWriter());
        assertEquals("141516\n242526\n343536\n", getOutput());
    }

    public void testFor() throws Exception
    {
        new For().render(getWriter());
        assertEquals("141516\n242526\n343536\n", getOutput());
    }
}
