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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.jamon.api.TemplateLocation;
import org.jamon.api.TemplateSource;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.compiler.TemplateResourceLocation;
import org.jamon.node.AnnotationNode;
import org.jamon.node.LocationImpl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class AnalyzerTest extends TestCase
{
    public static class MockTemplateSource implements TemplateSource
    {
        public MockTemplateSource(Map<String, String> p_content)
        {
            m_bytes = Maps.transformValues(p_content, new Function<String, byte[]>() {
                public byte[] apply(String p_input) { return p_input.getBytes(); }});
        }

        public long lastModified(String p_templatePath)
        {
            return 0;
        }

        public boolean available(String p_templatePath)
        {
            return m_bytes.containsKey(p_templatePath);
        }

        public InputStream getStreamFor(String p_templatePath)
        {
            return new ByteArrayInputStream(m_bytes.get(p_templatePath));
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
        {
            p_properties.put("org.jamon.alias.foo", "/x/y");
            p_properties.put("org.jamon.alias.bar", "/z/q");
            p_properties.put("org.jamon.escape", "j");
        }

        private final Map<String, byte[]> m_bytes;
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
        return analyze(ImmutableMap.of(PATH, p_templateText));
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

    public void testAliases() throws Exception
    {
        String templateText = "<& foo//baz &><& bar//hit/me &>";
        TemplateUnit unit = analyzeText(templateText);
        Collection<String> deps = unit.getTemplateDependencies();
        assertEquals(2, deps.size());
        assertTrue(deps.contains("/x/y/baz"));
        assertTrue(deps.contains("/z/q/hit/me"));
    }

    public void testEscapingDirective() throws Exception
    {
        TemplateUnit unit = analyzeText("<%escape #x><% i %>");
        WriteStatement statement = (WriteStatement) unit.getStatements().get(0);
        assertEquals(EscapingDirective.get("x"), getDefaultEscaping(statement));
    }

    public void testEscapingProperty() throws Exception
    {
        TemplateUnit unit = analyzeText("<% i %>");
        WriteStatement statement = (WriteStatement) unit.getStatements().get(0);
        assertEquals(EscapingDirective.get("j"), getDefaultEscaping(statement));
    }

    private Object getDefaultEscaping(WriteStatement statement) throws NoSuchFieldException, IllegalAccessException
    {
        Field field = WriteStatement.class.getDeclaredField("m_escapingDirective");
        field.setAccessible(true);
        Object object = field.get(statement);
        return object;
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

    public void testAnnotations() throws Exception
    {
        TemplateUnit templateUnit = analyzeText(
                    "a<%annotate @Foo #impl%>\n<%annotate @Bar#proxy %>\n<%annotate @Baz%>");
        assertEquals(
            Arrays.asList(
                new AnnotationNode(
                    location(1, 2),
                    "@Foo ", AnnotationType.IMPL),
                new AnnotationNode(
                    location(2, 1),
                    "@Bar", AnnotationType.PROXY),
                new AnnotationNode(
                    location(3, 1),
                    "@Baz", AnnotationType.BOTH)),
            templateUnit.getAnnotations());
    }

    public void testReplacing() throws Exception
    {
        TemplateUnit templateUnit = analyze(ImmutableMap.of(
            PATH, "<%replaces /foo>",
            "/foo", "<%replaceable>")); // TemplateDescriber will need to see the source for /foo
        assertEquals("/foo", templateUnit.getReplacedTemplatePath());
    }

    public void testAbstractReplacesError() throws Exception
    {
        analyzeExpectingErrors(
            ImmutableMap.of(PATH, "<%abstract><%replaces /foo>"),
            new ParserErrorImpl(
                location(1, 12),
                Analyzer.ABSTRACT_REPLACING_TEMPLATE_ERROR));
    }

    public void testMissingRequiredArgsInReplacement() throws Exception
    {
        analyzeExpectingErrors(
            ImmutableMap.of(
                PATH, "<%replaces /foo><%args>int i; int j;</%args>",
                "/foo", "<%replaceable>"),
            new ParserErrorImpl(
                location(1, 1),
                "Replaced template contains no required argument named i"),
            new ParserErrorImpl(
                location(1, 1),
                "Replaced template contains no required argument named j"));
    }

    public void testMissingFragsInReplacement() throws Exception
    {
        analyzeExpectingErrors(
            ImmutableMap.of(
                PATH, "<%replaces /foo><%frag f/>",
                "/foo", "<%replaceable>"),
                new ParserErrorImpl(
                    location(1,1),
                    "Replaced template contains no fragment argument named f"));
    }

    public void testMissingOptionalArgsInReplacement() throws Exception
    {
        analyzeExpectingErrors(
            ImmutableMap.of(
                PATH, "<%replaces /foo><%args>int i = 1; int j = 2; int k = 3;</%args>",
                "/foo", "<%replaceable><%args>int j = 2; int k;</%args>"),
                new ParserErrorImpl(
                    location(1, 1),
                    "Replaced template contains no required or optional argument named i"));
    }

    public void testCircularInheritance() throws Exception
    {
        analyzeExpectingErrors(
            ImmutableMap.of(PATH, "<%extends " + PATH + ">"),
            new ParserErrorImpl(location(1, 1), "cyclic inheritance or replacement involving " + PATH));
    }

    public void testCircularReplacement() throws Exception
    {
        analyzeExpectingErrors(
            ImmutableMap.of(PATH, "<%replaces " + PATH + ">"),
            new ParserErrorImpl(location(1, 1), "cyclic inheritance or replacement involving " + PATH));
    }

    public void testAbstractReplacing() throws Exception
    {
        analyzeExpectingErrors(
            ImmutableMap.of(PATH, "<%abstract>\n<%replaces /foo>", "/foo", "<%replaceable>"),
            new ParserErrorImpl(location(2,1), Analyzer.ABSTRACT_REPLACING_TEMPLATE_ERROR));
    }

    public void testAbstractReplaceable() throws Exception
    {
        analyzeExpectingErrors(
            ImmutableMap.of(PATH, "<%abstract>\n<%replaceable>"),
            new ParserErrorImpl(location(2,1), Analyzer.ABSTRACT_REPLACEABLE_TEMPLATE_ERROR));
    }

    public void testReplacingNonReplaceable() throws Exception
    {
        analyzeExpectingErrors(
            ImmutableMap.of(PATH, "<%replaces /foo>", "/foo", ""),
            new ParserErrorImpl(location(1,1), Analyzer.REPLACING_NON_REPLACEABLE_TEMPLATE_ERROR));
    }

    private void analyzeExpectingErrors(
        Map<String, String> p_contents, ParserErrorImpl... p_errors) throws IOException
    {
        try {
            analyze(p_contents);
            fail("Exception expected");
        }
        catch (ParserErrorsImpl e)
        {
            assertEquals(Arrays.asList(p_errors), e.getErrors());
        }
    }

    private TemplateUnit analyze(Map<String, String> p_contents) throws IOException {
        return new Analyzer(PATH, new TemplateDescriber(
            new MockTemplateSource(p_contents), getClass().getClassLoader())).analyze();
    }

    private LocationImpl location(int p_line, int p_column) {
        return new LocationImpl(new TemplateResourceLocation(PATH), p_line, p_column);
    }
}
