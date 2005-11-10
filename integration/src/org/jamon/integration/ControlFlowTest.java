package org.jamon.integration;

import test.jamon.For;
import test.jamon.PrimitiveIfTest;
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

    public void testIf() throws Exception
    {
        new PrimitiveIfTest().render(getWriter(), 0);
        assertEquals("i is 0\ni is 0\ni is 0", getOutput());

        resetWriter();

        new PrimitiveIfTest().render(getWriter(), 1);
        assertEquals("\ni is not 0\ni is positive", getOutput());

        resetWriter();

        new PrimitiveIfTest().render(getWriter(), -1);
        assertEquals("\ni is not 0\ni is negative", getOutput());
    }
}
