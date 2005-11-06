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

import java.io.IOException;
import java.io.StringReader;

import org.jamon.node.*;

/**
 * @author ian
 **/
public class ParserTest extends AbstractParserTest
{
    private static final String JAVA_END = "</%java>";
    private static final String JAVA_START = "<%java>";
    private static final String LITERAL_START = "<%LITERAL>";
    private static final String LITERAL_END = "</%LITERAL>";
    private static final String EMIT_START = "<% ";
    private static final String EMIT_END = "%>";

    private static final String BEFORE = "before";
    private static final String AFTER = "after";
    public ParserTest(String p_name)
    {
        super(p_name);
    }

    private void checkParseText(final String p_text) throws Exception
    {
        assertEquals(
            topNode().addSubNode(new TextNode(START_LOC, p_text)),
            new TopLevelParser(
                TEMPLATE_LOC, new StringReader(p_text)).getRootNode());
    }

    public void testParseText() throws Exception
    {
        checkParseText("Hello, world");
        checkParseText("Hello < world");
        checkParseText("Hello << world");
        checkParseText("Hello <");
    }

    public void testParseJavaLine() throws Exception
    {
        String java = "int c = 4;\n";
        assertEquals(
            topNode().addSubNode(new JavaNode(START_LOC, java)),
            parse("%" + java));
    }

    public void testParseTextAndJavaLine() throws Exception
    {
        String text = "hello, world\n";
        String java = "int c = 4;";
        assertEquals(
            topNode().addSubNode(
                new TextNode(START_LOC, text)).addSubNode(
                new JavaNode(location(2, 1), java)),
            new TopLevelParser(
                TEMPLATE_LOC, new StringReader(text + "%" + java))
                .getRootNode());
    }

    public void testParseJavaLineAndText() throws Exception
    {
        String text = "hello, world\n";
        String java = "int c = 4;\n";
        assertEquals(
            topNode().addSubNode(
                new JavaNode(START_LOC, java)).addSubNode(
                new TextNode(location(2, 1), text)),
            parse("%" + java + text));
    }

    public void testParseEscapedNewline() throws Exception
    {
        String line1 = "line\\1", line2 = "line2\\";
        assertEquals(
            topNode().addSubNode(
                new TextNode(location(1,1), line1 + line2)),
            parse(line1 + "\\\n" + line2));
    }

    public void testJavaTag() throws Exception
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

    public void testJavaTagWithQuotes() throws Exception
    {
        checkJavaString("String s = \" " + JAVA_END + "\"; ");
        checkJavaString("String s = \" \\\" " + JAVA_END + "\"; ");
        checkJavaString("String s = \"'" + JAVA_END + "\"; ");
        checkJavaString("String s = ' " + JAVA_END + " '; ");
        checkJavaString("int a = 3; \\");
    }

    public void testLiteral() throws Exception
    {
        String literal = LITERAL_START + JAVA_START + LITERAL_END;
        assertEquals(
            topNode()
                .addSubNode(new TextNode(START_LOC, BEFORE))
                .addSubNode(
                    new TextNode(
                        location(1, 1 + BEFORE.length()),
                        JAVA_START))
                .addSubNode(
                    new TextNode(
                        location(1, 1 + (BEFORE + literal).length()),
                        AFTER)),
            parse(BEFORE + literal + AFTER));
    }

    public void testFalseStartForJavaTagEnd() throws Exception
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

    public void testDef() throws Exception
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

    public void testMethod() throws Exception
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

    public void testAbsMethod() throws Exception
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

    public void testOverride() throws Exception
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

    public void testCall() throws Exception
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

    public void testEmit() throws Exception
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

    public void testEmitWithGreaterThan() throws Exception
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

    public void testClassTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
                new ClassNode(location(1,1), "foo")),
            parse("<%class>foo</%class>"));
    }

    public void testExtendsTag() throws Exception
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

    public void testEmptyImplementsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new ImplementsNode(START_LOC)),
            parse("<%implements></%implements>"));
    }

    public void testSingleImplementsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImplementsNode(location(1,1))
                   .addImplement(new ImplementNode(location(2,1), "a.b"))),
            parse("<%implements>\na.b;\n</%implements>"));
    }

    public void testMultiImplementsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImplementsNode(location(1,1))
                   .addImplement(new ImplementNode(location(2,1), "aa.bb"))
                   .addImplement(new ImplementNode(location(3,1), "c.d"))),
            parse("<%implements>\naa.bb;\nc.d;\n</%implements>"));
    }

    public void testImplementsWhitespaceTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImplementsNode(location(1,1))
                   .addImplement(new ImplementNode(location(2,1), "a.b"))),
            parse("<%implements>\na . b ;\n</%implements>"));
    }

    public void testEmptyImportsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new ImportsNode(START_LOC)),
            parse("<%import></%import>"));
    }

    public void testSingleImportsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImportsNode(location(1,1))
                   .addImport(new ImportNode(location(2,1), "a.b"))),
            parse("<%import>\na.b;\n</%import>"));
    }

    public void testMultiImportsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImportsNode(location(1,1))
                   .addImport(new ImportNode(location(2,1), "aa.bb"))
                   .addImport(new ImportNode(location(3,1), "c.*"))),
            parse("<%import>\naa.bb;\nc.*;\n</%import>"));
    }

    public void testImportsWhitespace() throws Exception
    {
        assertEquals(
            topNode().addSubNode(
               new ImportsNode(location(1,1))
                   .addImport(new ImportNode(location(2,1), "aa.bb"))
                   .addImport(new ImportNode(location(3,1), "c.*"))),
            parse("<%import>\naa . bb ;\nc . * ;\n</%import>"));
    }

    public void testEmptyParentArgsTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new ParentArgsNode(START_LOC)),
            parse("<%xargs></%xargs>"));
    }

    public void testParentArgsTag() throws Exception
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

    public void testParentArgsInOverrideTag() throws Exception
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

    public void testDocTag() throws Exception
    {
        String begining = "before<%doc>some docs</%doc>";
        assertEquals(
            topNode()
                .addSubNode(new TextNode(START_LOC, "before"))
                .addSubNode(new TextNode(location(1, begining.length() + 1),
                                         "after")),
            parse(begining + "after"));
    }

    public void testParentMarkerTag() throws Exception
    {
        assertEquals(
            topNode().addSubNode(new ParentMarkerNode(location(1,1))),
            parse("<%abstract>"));
    }

    public void testEscapeTag() throws Exception
    {
        assertEquals(
            topNode()
                .addSubNode(new EscapeDirectiveNode(location(1,1), "u")),
            parse("<%escape #u>"));
    }

    public void testWhileTag() throws Exception
    {
        final String whileTag = "<%while cond%>";
        assertEquals(
            topNode()
            .addSubNode(new WhileNode(location(1,1), "cond")
                .addSubNode(new TextNode(
                    location(1, 1 + whileTag.length()), "text"))),
            parse(whileTag + "text</%while>"));
    }

    public void testForTag() throws Exception
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
}