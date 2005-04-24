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

import java.util.regex.Pattern;

import org.jamon.node.DepthFirstAnalysisAdapter;
import org.jamon.node.TextNode;

public class WhitespaceSoakingTest extends AbstractParserTest
{
    public WhitespaceSoakingTest(String p_name)
    {
        super(p_name);
    }

    private static class WhitespaceSearcher extends DepthFirstAnalysisAdapter
    {
        private static final Pattern whitespace = Pattern.compile(".*\\s.*");
        
        public void caseTextNode(TextNode p_node)
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
    
    public void testWhitespaceAfterArgs() throws Exception
    {
        assertNoWhitespace("<%args></%args>  \n test");
    }
    
    public void testWhitespaceAfterDoc() throws Exception
    {
        assertNoWhitespace("<%doc> </%doc>  \n test");
    }
    
    public void testWhitespaceAfterFrag() throws Exception
    {
        assertNoWhitespace("<%frag f />  \n test");
        assertNoWhitespace("<%frag f> </%frag>  \n test");
    }
    
    public void testWhitespaceAfterXargs() throws Exception
    {
        assertNoWhitespace("<%xargs> </%xargs>  \n test");
    }
    
    public void testWhitespaceAfterAbstract() throws Exception
    {
        assertNoWhitespace("<%abstract>  \n test");
    }

    public void testWhitespaceAfterEscape() throws Exception
    {
        assertNoWhitespace("<%escape # u>  \n test");
    }
      
    public void testWhitespaceAfterExtends() throws Exception
    {
        assertNoWhitespace("<%extends foo>  \n test");
    }
    
    public void testWhitespaceAfterImport() throws Exception
    {
        assertNoWhitespace("<%import> </%import>  \n test");
    }

    public void testWhitespaceAfterImplements() throws Exception
    {
        assertNoWhitespace("<%implements> </%implements>  \n test");
    }

    public void testWhitespaceAfterJava() throws Exception
    {
        assertNoWhitespace("<%java> </%java>  \n test");
    }

    public void testWhitespaceAfterAlias() throws Exception
    {
        assertNoWhitespace("<%alias> </%alias>  \n test");
    }

    public void testWhitespaceAfterAbsmeth() throws Exception
    {
        assertNoWhitespace("<%absmeth foo></%absmeth>  \n test");
    }
    
    public void testWhitespaceAfterDef() throws Exception
    {
        assertNoWhitespace("<%def foo></%def>  \n test");
    }
    
    public void testWhitespaceAfterMethod() throws Exception
    {
        assertNoWhitespace("<%method foo></%method>  \n test");
    }
    
    public void testWhitespaceAfterOvereride() throws Exception
    {
        assertNoWhitespace("<%override foo></%override>  \n test");
    }
}
