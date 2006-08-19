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

package org.jamon.codegen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.jamon.TemplateLocation;
import org.jamon.TemplateResourceLocation;
import org.jamon.TemplateSource;
import junit.framework.TestCase;

public class AnalyzerTest extends TestCase
{
    public static class MockTemplateSource implements TemplateSource
    {
        public MockTemplateSource(String p_content)
        {
            m_bytes = p_content.getBytes();
        }

        public long lastModified(String p_templatePath)
        {
            return 0;
        }

        public boolean available(String p_templatePath)
        {
            return false;
        }

        public InputStream getStreamFor(String p_templatePath)
        {
            return new ByteArrayInputStream(m_bytes);
        }

        public String getExternalIdentifier(String p_templatePath)
        {
            return p_templatePath;
        }

        public TemplateLocation getTemplateLocation(String p_templatePath)
        {
            return new TemplateResourceLocation(p_templatePath);
        }

        public void loadProperties(String p_path, Properties p_properties)
        {}

        private final byte[] m_bytes;
    }

    private static String PATH = "/test";

    private void checkTypes(List<Statement> p_list, Class<?> ...p_classes)
    {
        assertEquals(p_classes.length, p_list.size());
        int i = 0;
        for (Class<?> clazz : p_classes)
        {
            assertEquals(clazz, p_list.get(i++).getClass());
        }
    }

    private void assertStatementText(String p_expected, Statement p_statement)
    {
        assertEquals(p_expected, ((LiteralStatement) p_statement).getText());
    }

    private TemplateUnit analyzeText(String p_templateText) throws IOException
    {
        return new Analyzer(
            PATH,
            new TemplateDescriber(new MockTemplateSource(p_templateText),
                                  getClass().getClassLoader()))
        .analyze();
    }

    private void checkLoopBlock(
        Class<? extends AbstractStatementBlock> p_loopClass, String p_loopName)
        throws IOException
    {
        String templateText = "a<%" + p_loopName + " l%>b<% x %>c</%"
                        + p_loopName + ">d";
        TemplateUnit unit = analyzeText(templateText);
        checkTypes(
            unit.getStatements(),
            LiteralStatement.class, p_loopClass, LiteralStatement.class);
        assertStatementText("a", unit.getStatements().get(0));
        assertStatementText("d", unit.getStatements().get(2));
        AbstractStatementBlock loopBlock =
            (AbstractStatementBlock) unit.getStatements().get(1);
        checkTypes(
            loopBlock.getStatements(),
            LiteralStatement.class, WriteStatement.class, LiteralStatement.class);
        assertStatementText("b", loopBlock.getStatements().get(0));
        assertStatementText("c", loopBlock.getStatements().get(2));
    }

    public void testForBlock() throws Exception
    {
        checkLoopBlock(FlowControlBlock.class, "for");
    }

    public void testWhileBlock() throws Exception
    {
        checkLoopBlock(FlowControlBlock.class, "while");
    }

    public void testTextCompactification() throws Exception
    {
        TemplateUnit unit = analyzeText("a<%def d></%def>b");
        checkTypes(unit.getStatements(), LiteralStatement.class);
        assertStatementText("ab", unit.getStatements().get(0));
    }

    public void testLiteralCompactification() throws Exception
    {
        TemplateUnit unit = analyzeText("a<%LITERAL>b\n</%LITERAL>\nc");
        checkTypes(unit.getStatements(), LiteralStatement.class);
        assertStatementText("ab\n\nc", unit.getStatements().get(0));
    }
}
