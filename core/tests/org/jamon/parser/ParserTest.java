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

import java.io.IOException;
import java.io.StringReader;

import org.jamon.node.*;
import org.junit.Test;

/**
 * @author ian
 **/
public class ParserTest extends AbstractParserTest
{
    private static final String JAVA_SNIPPET_START = "<%java ";
    private static final String JAVA_END = "</%java>";
    private static final String JAVA_START = "<%java>";
    private static final String LITERAL_START = "<%LITERAL>";
    private static final String LITERAL_END = "</%LITERAL>";
    private static final String EMIT_START = "<% ";
    private static final String EMIT_END = "%>";

    private static final String BEFORE = "before";
    private static final String AFTER = "after";

    private void checkParseText(final String p_text) throws Exception
    {
        assertEquals(
            topNode().addSubNode(new TextNode(START_LOC, p_text)),
            new TopLevelParser(
                TEMPLATE_LOC, new StringReader(p_text)).parse().getRootNode());
    }

    @Test public void testParseText() throws Exception
    {
        checkParseText("Hello, world");
        checkParseText("Hello < world");
        checkParseText("Hello << world");
        checkParseText("Hello <");
    }

    @Test public void testParseJavaLine() throws Exception
    {
        String java = "int c = 4;\n";
        assertEquals(
            topNode().addSubNode(new JavaNode(START_LOC, java)),
            parse("%" + java));
    }

    @Test public void testParseTextAndJavaLine() throws Exception
    {
        String text = "hello, world\n";
        String java = "int c = 4;";
        assertEquals(
            topNode().addSubNode(
                new TextNode(START_LOC, text)).addSubNode(
                new JavaNode(location(2, 1), java)),
            new TopLevelParser(
                TEMPLATE_LOC, new StringReader(text + "%" + java))
                .parse()
                .getRootNode());
    }

    @Test public void testParseJavaLineAndText() throws Exception
    {
        String text = "hello, world\n";
        String java = "int c = 4;\n";
        assertEquals(
            topNode().addSubNode(
                new JavaNode(START_LOC, java)).addSubNode(
                new TextNode(location(2, 1), text)),
            parse("%" + java + text));
    }

    @Test public void testParseJavaSnippet() throws Exception
    {
        String java = "int c = 4;";
        assertEquals(
            topNode().addSubNode(new JavaNode(START_LOC, java)),
            parse(JAVA_SNIPPET_START + java + EMIT_END));
    }

    @Test public void testParseJavaSnippetInTextWithQuotes() throws Exception
    {
        String text1 = "hello\n", text2 = "goodbye", java = "String c = \"a%%>\";";
        assertEquals(
            topNode()
                .addSubNode(new TextNode(START_LOC, text1))
                .addSubNode(new JavaNode(location(2,1), java))
                .addSubNode(new TextNode(
                    location(2, java.length() + JAVA_SNIPPET_START.length() + EMIT_END.length() + 1),
                    text2)),
            parse(text1 + JAVA_SNIPPET_START + java + EMIT_END + text2));
    }

    
    @Test public void testParseEscapedNewline() throws Exception
    {
        String line1 = "line\\1", line2 = "line2\\";
        assertEquals(
            topNode().addSubNode(
                new TextNode(location(1,1), line1 + line2)),
            parse(line1 + "\\\n" + line2));
    }

    @Test public void testJavaTag() throws Exception
    {
        String java = "int c = 4;";
        String javaTag = JAVA_START + java + JAVA_END;
        assertEquals(
            topNode()
                .addSubNode(new TextNode(START_LOC, BEFORE))
                .addSubNode(
                    new JavaNode(location(1, 1 + BEFORE.length()), java))
                .addSubNode(
                    new TextNode(
                        location(1, 1 + (BEFORE + javaTag).length()),
                        AFTER)),
            parse(BEFORE + javaTag + AFTER));
    }

    private void checkJavaString(String p_java) throws Exception
    {
        assertEquals(
            topNode().addSubNode(new JavaNode(START_LOC, p_java)),
            parse(JAVA_START + p_java + JAVA_END));
    }

    @Test public void testJavaTagWithQuotes() throws Exception
    {
        checkJavaString("String s = \" " + JAVA_END + "\"; ");
        checkJavaString("String s = \" \\\" " + JAVA_END + "\"; ");
        checkJavaString("String s = \"'" + JAVA_END + "\"; ");
        checkJavaString("String s = ' " + JAVA_END + " '; ");
        checkJavaString("int a = 3; \\");
    }

    @Test public void testLiteral() throws Exception
    {
        String literal = LITERAL_START + JAVA_START + LITERAL_END;
        assertEquals(
            topNode()
                .addSubNode(new TextNode(START_LOC, BEFORE))
                .addSubNode(
                    new LiteralNode(
                        location(1, 1 + BEFORE.length()),
                        JAVA_START))
                .addSubNode(
                    new TextNode(
                        location(1, 1 + (BEFORE + literal).length()),
                        AFTER)),
            parse(BEFORE + literal + AFTER));
    }

    @Test public void testFalseStartForJavaTagEnd() throws Exception
    {
        checkJavaString("a </% b");
    }

    private interface SubcomponentBuilder
    {
        SubcomponentNode makeNode(Location p_location, String p_name);
    }

    private void doSubcomponentTest(
            String p_prefix, String p_suffix, SubcomponentBuilder p_builder)
        throws IOException
    {
        String during = "during";
        String name = "foo";
        String start = p_prefix + " " + name + ">";
        String subcomponent = start + during + p_suffix;
        assertEquals(
            topNode()
                .addSubNode(new TextNode(START_LOC, BEFORE))
                .addSubNode(
                    p_builder
                        .makeNode(location(1, 1 + BEFORE.length()),
                                  name)
                        .addSubNode(new TextNode(
                            location(1, 1 + (BEFORE + start).length()),
                            during)))
                .addSubNode(
                    new TextNode(
                        location(1, 1 + (BEFORE + subcomponent).length()),
                        AFTER)),
            parse(BEFORE + subcomponent + AFTER));
    }

    @Test public void testDef() throws Exception
    {
        doSubcomponentTest(
            "<%def", "</%def>",
            new SubcomponentBuilder() {
                public SubcomponentNode makeNode(Location p_location,
                                                 String p_name)
                {
                    return new DefNode(p_location, p_name);
                }

            });
    }

    @Test public void testMethod() throws Exception
    {
        doSubcomponentTest(
            "<%method", "</%method>",
            new SubcomponentBuilder() {
                public SubcomponentNode makeNode(Location p_location,
                                                 String p_name)
                {
                    return new MethodNode(p_location, p_name);
                }

            });
    }

    @Test public void testAbsMethod() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(
                    new AbsMethodNode(START_LOC, "foo")
                        .addArgsBlock(
                            new ArgsNode(location(2,1))
                                .addArg(
                                    new ArgNode(location(3,1),
                                    new ArgTypeNode(location(3,1), "int"),
                                    new ArgNameNode(location(3, 5), "i"))))
                        .addArgsBlock(
                            new FragmentArgsNode(location(5,1), "f"))),
             parse("<%absmeth foo>\n<%args>\nint i;\n</%args>\n<%frag f/></%absmeth>"));
    }

    @Test public void testOverride() throws Exception
    {
        doSubcomponentTest(
            "<%override", "</%override>",
            new SubcomponentBuilder() {
                public SubcomponentNode makeNode(Location p_location,
                                                 String p_name)
                {
                    return new OverrideNode(p_location, p_name);
                }

            });
    }

    @Test public void testCall() throws Exception
    {
        String call = "<& foo &>";
        Location pathStart = location(1, (BEFORE + "<& f").length());
        assertEquals(
            topNode()
                .addSubNode(new TextNode(START_LOC, BEFORE))
                .addSubNode(
                    new SimpleCallNode(
                        location(1, BEFORE.length() + 1),
                        new RelativePathNode(pathStart).addPathElement(
                            new PathElementNode(pathStart, "foo")),
                        new NoParamsNode(
                            location(1, (BEFORE + "<& foo &").length()))))
                .addSubNode(
                    new TextNode(
                        location(1, (BEFORE + call).length() + 1),
                        AFTER)),
            parse(BEFORE + call + AFTER));
    }

    @Test public void testEmit() throws Exception
    {
        String emitExpr = "foo";
        String emit = EMIT_START + emitExpr + EMIT_END;
        assertEquals(
            topNode()
                .addSubNode(new TextNode(START_LOC, BEFORE))
                .addSubNode(
                    new EmitNode(
                        location(1, 1 + BEFORE.length()),
                        emitExpr,
                        new DefaultEscapeNode(
                            location(1, (BEFORE + emit).length()))))
                .addSubNode(
                    new TextNode(
                        location(1, 1 + (BEFORE + emit).length()),
                        AFTER)),
            parse(BEFORE + emit + AFTER));
        String escapedEmit = EMIT_START + emitExpr + "#h" + EMIT_END;
        assertEquals(
            topNode().addSubNode(
                new TextNode(START_LOC, BEFORE)).addSubNode(
                new EmitNode(
                    location(1, 1 + BEFORE.length()),
                    emitExpr,
                    new EscapeNode(
                        location(
                            1,
                            1 + (BEFORE + EMIT_START + emitExpr).length()),
                        "h"))).addSubNode(
                    new TextNode(
                        location(1, 1 + (BEFORE + escapedEmit).length()),
                        AFTER)),
                parse(BEFORE + escapedEmit + AFTER));
    }

    @Test public void testEmitWithGreaterThan() throws Exception
    {
       String emitExpr = "foo > bar";
       String emit = EMIT_START + emitExpr + EMIT_END;
       assertEquals(
           topNode()
               .addSubNode(new TextNode(START_LOC, BEFORE))
               .addSubNode(
                   new EmitNode(
                       location(1, 1 + BEFORE.length()),
                       emitExpr,
                       new DefaultEscapeNode(
                           location(1, (BEFORE + emit).length()))))
               .addSubNode(
                   new TextNode(
                       location(1, 1 + (BEFORE + emit).length()),
                       AFTER)),
           parse(BEFORE + emit + AFTER));
    }

    @Test public void testClassTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
                new ClassNode(location(1,1), "foo")),
            parse("<%class>foo</%class>"));
    }

    @Test public void testExtendsTag() throws Exception
    {
        String path = "/foo/bar";
        String extendsTagStart = "<%extends ";
        assertEquals(
            topNode().addSubNode(
                new ExtendsNode(
                    location(1,1),
                    buildPath(
                        location(1, extendsTagStart.length() + 2),
                        new AbsolutePathNode(
                            location(1, extendsTagStart.length() + 1)),
                        path))),
            parse(extendsTagStart + path + ">"));
    }

    @Test public void testEmptyImplementsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new ImplementsNode(START_LOC)),
            parse("<%implements></%implements>"));
    }

    @Test public void testSingleImplementsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImplementsNode(location(1,1))
                   .addImplement(new ImplementNode(location(2,1), "a.b"))),
            parse("<%implements>\na.b;\n</%implements>"));
    }

    @Test public void testMultiImplementsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImplementsNode(location(1,1))
                   .addImplement(new ImplementNode(location(2,1), "aa.bb"))
                   .addImplement(new ImplementNode(location(3,1), "c.d"))),
            parse("<%implements>\naa.bb;\nc.d;\n</%implements>"));
    }

    @Test public void testImplementsWhitespaceTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImplementsNode(location(1,1))
                   .addImplement(new ImplementNode(location(2,1), "a.b"))),
            parse("<%implements>\na . b ;\n</%implements>"));
    }

    @Test public void testEmptyImportsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new ImportsNode(START_LOC)),
            parse("<%import></%import>"));
    }

    @Test public void testSingleImportsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImportsNode(location(1,1))
                   .addImport(new ImportNode(location(2,1), "a.b"))),
            parse("<%import>\na.b;\n</%import>"));
    }

    @Test public void testMultiImportsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImportsNode(location(1,1))
                   .addImport(new ImportNode(location(2,1), "aa.bb"))
                   .addImport(new ImportNode(location(3,1), "c.*"))),
            parse("<%import>\naa.bb;\nc.*;\n</%import>"));
    }

    @Test public void testImportsWhitespace() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImportsNode(location(1,1))
                   .addImport(new ImportNode(location(2,1), "aa.bb"))
                   .addImport(new ImportNode(location(3,1), "c.*"))),
            parse("<%import>\naa . bb ;\nc . * ;\n</%import>"));
    }

    @Test public void testEmptyParentArgsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new ParentArgsNode(START_LOC)),
            parse("<%xargs></%xargs>"));
    }

    @Test public void testParentArgsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
                new ParentArgsNode(START_LOC)
                    .addArg(new ParentArgNode(
                        location(2,1),
                        new ArgNameNode(location(2, 1), "a")))
                    .addArg(new ParentArgWithDefaultNode(
                        location(3,1),
                        new ArgNameNode(location(3,1), "b"),
                        new ArgValueNode(location(3, 6), "x")))),
            parse("<%xargs>\na;\nb => x;</%xargs>"));
    }

    @Test public void testParentArgsInOverrideTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
                new OverrideNode(START_LOC, "m").addSubNode(
                    new ParentArgsNode(location(2, 1))
                        .addArg(new ParentArgNode(
                            location(3,1),
                            new ArgNameNode(location(3, 1), "a")))
                        .addArg(new ParentArgWithDefaultNode(
                            location(4,1),
                            new ArgNameNode(location(4,1), "b"),
                            new ArgValueNode(location(4, 6), "x"))))),
            parse("<%override m>\n<%xargs>\na;\nb => x;</%xargs></%override>"));
    }

    @Test public void testDocTag() throws Exception
    {
        String begining = BEFORE + "<%doc>some docs</%doc>";
        assertEquals(
            topNode()
                .addSubNode(new TextNode(START_LOC, BEFORE))
                .addSubNode(
                    new DocNode(location(1, BEFORE.length() + 1), "some docs"))
                .addSubNode(new TextNode(location(1, begining.length() + 1),
                                         "after")),
            parse(begining + "after"));
    }

    @Test public void testParentMarkerTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new ParentMarkerNode(location(1,1))),
            parse("<%abstract>"));
    }

    @Test public void testEscapeTag() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new EscapeDirectiveNode(location(1,1), "u")),
            parse("<%escape #u>"));
    }

    @Test public void testWhileTag() throws Exception
    {
        final String whileTag = "<%while cond%>";
        assertEquals(
            topNode()
            .addSubNode(new WhileNode(location(1,1), "cond")
                .addSubNode(new TextNode(
                    location(1, 1 + whileTag.length()), "text"))),
            parse(whileTag + "text</%while>"));
    }

    @Test public void testForTag() throws Exception
    {
        final String forTag = "<%for loop : baz%>";
        assertEquals(
            topNode()
            .addSubNode(new ForNode(location(1,1), "loop : baz")
                .addSubNode(new EmitNode(
                    location(
                        1,
                        1 + forTag.length()), "x ",
                        new DefaultEscapeNode(location(
                            1, 1 + forTag.length() + "<% x %".length()))))
                .addSubNode(new TextNode(
                    location(1, 1 + forTag.length() + "<% x %>".length()),
                    "text"))),
            parse(forTag + "<% x %>text</%for>"));
    }

    @Test public void testIfTag() throws Exception
    {
        final String ifTag = "<%if cond%>";
        assertEquals(
            topNode()
            .addSubNode(new IfNode(location(1,1), "cond")
                .addSubNode(new TextNode(
                    location(1, 1 + ifTag.length()), "text"))),
            parse(ifTag + "text</%if>"));
    }

    @Test public void testIfElseTags() throws Exception
    {
        final String ifTag = "<%if cond%>";
        assertEquals(
            topNode()
            .addSubNode(new IfNode(location(1,1), "cond")
                .addSubNode(new TextNode(
                    location(1, 1 + ifTag.length()), "text\n")))
            .addSubNode(new ElseNode(location(2,1))
                .addSubNode(new TextNode(location(2, 8), "other"))),
            parse(ifTag + "text\n<%else>other</%if>"));
    }

    @Test public void testIfElseIfTags() throws Exception
    {
        final String ifTag = "<%if cond%>";
        final String elseIfTag = "<%elseif cond2%>";
        assertEquals(
            topNode()
            .addSubNode(new IfNode(location(1,1), "cond")
                .addSubNode(new TextNode(
                    location(1, 1 + ifTag.length()), "text\n")))
            .addSubNode(new ElseIfNode(location(2,1), "cond2")
                .addSubNode(
                    new TextNode(location(2, 1 + elseIfTag.length()), "other"))),
            parse(ifTag + "text\n" + elseIfTag + "other</%if>"));
    }

    @Test public void testIfElseIfElseTags() throws Exception
    {
        final String ifTag = "<%if cond%>";
        final String elseIfTag = "<%elseif cond2%>";
        assertEquals(
            topNode()
            .addSubNode(new IfNode(location(1,1), "cond")
                .addSubNode(new TextNode(
                    location(1, 1 + ifTag.length()), "text\n")))
            .addSubNode(new ElseIfNode(location(2,1), "cond2")
                .addSubNode(new TextNode(
                    location(2, 1 + elseIfTag.length()), "other\n")))
            .addSubNode(new ElseNode(location(3, 1))
                .addSubNode(new TextNode(location(3, 8), "third"))),
            parse(ifTag + "text\n" + elseIfTag + "other\n<%else>third</%if>"));
    }

    @Test public void testMultipleElseIfTags() throws Exception
    {
        final String ifTag = "<%if cond%>";
        final String elseIfTag1 = "<%elseif cond1%>";
        final String elseIfTag2 = "<%elseif cond2%>";
        assertEquals(
            topNode()
            .addSubNode(new IfNode(location(1,1), "cond")
                .addSubNode(new TextNode(
                    location(1, 1 + ifTag.length()), "text\n")))
            .addSubNode(new ElseIfNode(location(2,1), "cond1")
                .addSubNode(new TextNode(
                    location(2, 1 + elseIfTag1.length()), "one\n")))
            .addSubNode(new ElseIfNode(location(3,1), "cond2")
                .addSubNode(
                    new TextNode(location(3, 1 + elseIfTag2.length()), "two"))),
            parse(
                ifTag + "text\n"
                + elseIfTag1 + "one\n"
                + elseIfTag2 + "two</%if>"));
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.JUnit4TestAdapter(ParserTest.class);
    }
}