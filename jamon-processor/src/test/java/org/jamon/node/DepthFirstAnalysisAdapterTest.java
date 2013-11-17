/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.node;

import org.jamon.api.Location;
import org.jamon.compiler.TemplateFileLocation;

import junit.framework.TestCase;

public class DepthFirstAnalysisAdapterTest extends TestCase {
  private static final String PATH = "foo";

  private static final String[] VALUES = { "value1", "value2" };

  class TestAnalyzer extends DepthFirstAnalysisAdapter {
    @Override
    public void inTopNode(TopNode node) {
      assertFalse(inTopNodeCalled);
      assertFalse(outTopNodeCalled);
      inTopNodeCalled = true;
    }

    @Override
    public void outTopNode(TopNode node) {
      assertTrue(inTopNodeCalled);
      assertFalse(outTopNodeCalled);
      outTopNodeCalled = true;
    }

    @Override
    public void casePathElementNode(PathElementNode node) {
      assertFalse(casePathElementCalled);
      casePathElementCalled = true;
      assertEquals(PATH, node.getName());
    }

    @Override
    public void caseParamValueNode(ParamValueNode node) {
      assertTrue(paramsSeen < 2);
      assertEquals(VALUES[paramsSeen], node.getValue());
      paramsSeen++;
    }

    boolean inTopNodeCalled = false;
    boolean outTopNodeCalled = false;
    boolean casePathElementCalled = false;
    int paramsSeen = 0;
  }

  public void testAnalysis() {
    Location loc = new LocationImpl(new TemplateFileLocation("x"), 1, 1);
    TopNode top = (TopNode) new TopNode(loc, "US-ASCII").addSubNode(
      new SimpleCallNode(
        loc,
        new AbsolutePathNode(loc).addPathElement(new PathElementNode(loc, PATH)),
        new UnnamedParamsNode(loc)
          .addValue(new ParamValueNode(loc, VALUES[0]))
          .addValue(new ParamValueNode(loc, VALUES[1]))));
    TestAnalyzer testAnalyzer = new TestAnalyzer();
    top.apply(testAnalyzer);
    assertTrue(testAnalyzer.inTopNodeCalled);
    assertTrue(testAnalyzer.outTopNodeCalled);
    assertTrue(testAnalyzer.casePathElementCalled);
    assertEquals(2, testAnalyzer.paramsSeen);
  }
}
