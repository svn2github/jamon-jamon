/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */
package org.jamon.parser;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.jamon.node.DepthFirstAnalysisAdapter;
import org.jamon.node.TextNode;
import org.junit.Test;

public class WhitespaceSoakingTest extends AbstractParserTest
{
    private static class WhitespaceSearcher extends DepthFirstAnalysisAdapter
    {
        private static final Pattern whitespace = Pattern.compile(".*\\s.*");

        @Override public void caseTextNode(TextNode p_node)
        {
            if (whitespace.matcher(p_node.getText()).matches())
            {
                m_seenWhitespace = true;
            }
        }

        public boolean seenWhitespace()
        {
            return m_seenWhitespace;
        }

        private boolean m_seenWhitespace = false;
    }

    private void assertNoWhitespace(String p_document) throws Exception
    {
        WhitespaceSearcher searcher = new WhitespaceSearcher();
        parse(p_document).apply(searcher);
        assertFalse(searcher.seenWhitespace());
    }

    @Test public void testWhitespaceAfterArgs() throws Exception
    {
        assertNoWhitespace("<%args></%args>  \n test");
    }

    @Test public void testWhitespaceAfterDoc() throws Exception
    {
        assertNoWhitespace("<%doc> </%doc>  \n test");
    }

    @Test public void testWhitespaceAfterFrag() throws Exception
    {
        assertNoWhitespace("<%frag f />  \n test");
        assertNoWhitespace("<%frag f> </%frag>  \n test");
    }

    @Test public void testWhitespaceAfterXargs() throws Exception
    {
        assertNoWhitespace("<%xargs> </%xargs>  \n test");
    }

    @Test public void testWhitespaceAfterAbstract() throws Exception
    {
        assertNoWhitespace("<%abstract>  \n test");
    }

    @Test public void testWhitespaceAfterEscape() throws Exception
    {
        assertNoWhitespace("<%escape # u>  \n test");
    }

    @Test public void testWhitespaceAfterExtends() throws Exception
    {
        assertNoWhitespace("<%extends foo>  \n test");
    }

    @Test public void testWhitespaceAfterImport() throws Exception
    {
        assertNoWhitespace("<%import> </%import>  \n test");
    }

    @Test public void testWhitespaceAfterImplements() throws Exception
    {
        assertNoWhitespace("<%implements> </%implements>  \n test");
    }

    @Test public void testWhitespaceAfterJava() throws Exception
    {
        assertNoWhitespace("<%java> </%java>  \n test");
    }

    @Test public void testWhitespaceAfterAlias() throws Exception
    {
        assertNoWhitespace("<%alias> </%alias>  \n test");
    }

    @Test public void testWhitespaceAfterAbsmeth() throws Exception
    {
        assertNoWhitespace("<%absmeth foo></%absmeth>  \n test");
    }

    @Test public void testWhitespaceAfterDef() throws Exception
    {
        assertNoWhitespace("<%def foo></%def>  \n test");
    }

    @Test public void testWhitespaceAfterMethod() throws Exception
    {
        assertNoWhitespace("<%method foo></%method>  \n test");
    }

    @Test public void testWhitespaceAfterOvereride() throws Exception
    {
        assertNoWhitespace("<%override foo></%override>  \n test");
    }

    @Test public void testWhitespaceAfterGeneric() throws Exception
    {
       assertNoWhitespace("<%generic> T </%generic>  \n test");
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.JUnit4TestAdapter(WhitespaceSoakingTest.class);
    }
}
