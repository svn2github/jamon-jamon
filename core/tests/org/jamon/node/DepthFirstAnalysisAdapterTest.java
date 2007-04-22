package org.jamon.node;

import org.jamon.TemplateFileLocation;
import org.jamon.api.Location;

import junit.framework.TestCase;

public class DepthFirstAnalysisAdapterTest extends TestCase
{
    private static final String s_path = "foo";
    private static final String[] s_values = {"value1", "value2"};

    class TestAnalyzer extends DepthFirstAnalysisAdapter
    {
        @Override public void inTopNode(TopNode p_node)
        {
            assertFalse(inTopNodeCalled);
            assertFalse(outTopNodeCalled);
            inTopNodeCalled = true;
        }

        @Override public void outTopNode(TopNode p_node)
        {
            assertTrue(inTopNodeCalled);
            assertFalse(outTopNodeCalled);
            outTopNodeCalled = true;
        }

        @Override public void casePathElementNode(PathElementNode p_node)
        {
            assertFalse(casePathElementCalled);
            casePathElementCalled = true;
            assertEquals(s_path, p_node.getName());
        }

        @Override public void caseParamValueNode(ParamValueNode p_node)
        {
            assertTrue(paramsSeen < 2);
            assertEquals(s_values[paramsSeen], p_node.getValue());
            paramsSeen++;
        }

        boolean inTopNodeCalled = false;
        boolean outTopNodeCalled = false;
        boolean casePathElementCalled = false;
        int paramsSeen = 0;
    }

    public void testAnalysis()
    {
        Location loc = new LocationImpl(new TemplateFileLocation("x"), 1,1);
        TopNode top = (TopNode) new TopNode(loc)
            .addSubNode(new SimpleCallNode(
                loc,
                new AbsolutePathNode(loc).addPathElement(
                    new PathElementNode(loc, s_path)),
                new UnnamedParamsNode(loc)
                    .addValue(new ParamValueNode(loc, s_values[0]))
                    .addValue(new ParamValueNode(loc, s_values[1]))));
        TestAnalyzer testAnalyzer= new TestAnalyzer();
        top.apply(testAnalyzer);
        assertTrue(testAnalyzer.inTopNodeCalled);
        assertTrue(testAnalyzer.outTopNodeCalled);
        assertTrue(testAnalyzer.casePathElementCalled);
        assertEquals(2, testAnalyzer.paramsSeen);
    }
}
