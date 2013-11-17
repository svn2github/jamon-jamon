/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.parser;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.jamon.node.DepthFirstAnalysisAdapter;
import org.jamon.node.TextNode;
import org.junit.Test;

public class WhitespaceSoakingTest extends AbstractParserTest {
  private static class WhitespaceSearcher extends DepthFirstAnalysisAdapter {
    private static final Pattern whitespace = Pattern.compile(".*\\s.*");

    @Override
    public void caseTextNode(TextNode node) {
      if (whitespace.matcher(node.getText()).matches()) {
        m_seenWhitespace = true;
      }
    }

    public boolean seenWhitespace() {
      return m_seenWhitespace;
    }

    private boolean m_seenWhitespace = false;
  }

  private void assertNoWhitespace(String document) throws Exception {
    WhitespaceSearcher searcher = new WhitespaceSearcher();
    parse(document).apply(searcher);
    assertFalse(searcher.seenWhitespace());
  }

  @Test
  public void testWhitespaceAfterArgs() throws Exception {
    assertNoWhitespace("<%args></%args>  \n test");
  }

  @Test
  public void testWhitespaceAfterDoc() throws Exception {
    assertNoWhitespace("<%doc> </%doc>  \n test");
  }

  @Test
  public void testWhitespaceAfterFrag() throws Exception {
    assertNoWhitespace("<%frag f />  \n test");
    assertNoWhitespace("<%frag f> </%frag>  \n test");
  }

  @Test
  public void testWhitespaceAfterXargs() throws Exception {
    assertNoWhitespace("<%xargs> </%xargs>  \n test");
  }

  @Test
  public void testWhitespaceAfterAbstract() throws Exception {
    assertNoWhitespace("<%abstract>  \n test");
  }

  @Test
  public void testWhitespaceAfterEscape() throws Exception {
    assertNoWhitespace("<%escape # u>  \n test");
  }

  @Test
  public void testWhitespaceAfterExtends() throws Exception {
    assertNoWhitespace("<%extends foo>  \n test");
  }

  @Test
  public void testWhitespaceAfterImport() throws Exception {
    assertNoWhitespace("<%import> </%import>  \n test");
  }

  @Test
  public void testWhitespaceAfterImplements() throws Exception {
    assertNoWhitespace("<%implements> </%implements>  \n test");
  }

  @Test
  public void testWhitespaceAfterJava() throws Exception {
    assertNoWhitespace("<%java> </%java>  \n test");
  }

  @Test
  public void testWhitespaceAfterAlias() throws Exception {
    assertNoWhitespace("<%alias> </%alias>  \n test");
  }

  @Test
  public void testWhitespaceAfterAbsmeth() throws Exception {
    assertNoWhitespace("<%absmeth foo></%absmeth>  \n test");
  }

  @Test
  public void testWhitespaceAfterDef() throws Exception {
    assertNoWhitespace("<%def foo></%def>  \n test");
  }

  @Test
  public void testWhitespaceAfterMethod() throws Exception {
    assertNoWhitespace("<%method foo></%method>  \n test");
  }

  @Test
  public void testWhitespaceAfterOvereride() throws Exception {
    assertNoWhitespace("<%override foo></%override>  \n test");
  }

  @Test
  public void testWhitespaceAfterGeneric() throws Exception {
    assertNoWhitespace("<%generic> T </%generic>  \n test");
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(WhitespaceSoakingTest.class);
  }
}
