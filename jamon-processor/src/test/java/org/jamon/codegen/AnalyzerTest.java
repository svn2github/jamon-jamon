/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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

public class AnalyzerTest extends TestCase {
  public static class MockTemplateSource implements TemplateSource {
    public MockTemplateSource(Map<String, String> content) {
      bytes = Maps.transformValues(content, new Function<String, byte[]>() {
        @Override
        public byte[] apply(String input) {
          return input.getBytes();
        }
      });
    }

    @Override
    public long lastModified(String templatePath) {
      return 0;
    }

    @Override
    public boolean available(String templatePath) {
      return bytes.containsKey(templatePath);
    }

    @Override
    public InputStream getStreamFor(String templatePath) {
      return new ByteArrayInputStream(bytes.get(templatePath));
    }

    @Override
    public String getExternalIdentifier(String templatePath) {
      return templatePath;
    }

    @Override
    public TemplateLocation getTemplateLocation(String templatePath) {
      return new TemplateResourceLocation(templatePath);
    }

    @Override
    public void loadProperties(String path, Properties properties) {
      properties.put("org.jamon.alias.foo", "/x/y");
      properties.put("org.jamon.alias.bar", "/z/q");
      properties.put("org.jamon.escape", "j");
    }

    private final Map<String, byte[]> bytes;
  }

  private static String PATH = "/test";

  private void checkTypes(List<Statement> list, Class<?>... classes) {
    assertEquals(classes.length, list.size());
    int i = 0;
    for (Class<?> clazz : classes) {
      assertEquals(clazz, list.get(i++).getClass());
    }
  }

  private void assertStatementText(String expected, Statement statement) {
    assertEquals(expected, ((LiteralStatement) statement).getText());
  }

  private TemplateUnit analyzeText(String templateText) throws IOException {
    return analyze(ImmutableMap.of(PATH, templateText));
  }

  private void checkLoopBlock(Class<? extends AbstractStatementBlock> loopClass, String loopName)
  throws IOException {
    String templateText = "a<%" + loopName + " l%>b<% x %>c</%" + loopName + ">d";
    TemplateUnit unit = analyzeText(templateText);
    checkTypes(unit.getStatements(), LiteralStatement.class, loopClass, LiteralStatement.class);
    assertStatementText("a", unit.getStatements().get(0));
    assertStatementText("d", unit.getStatements().get(2));
    AbstractStatementBlock loopBlock = (AbstractStatementBlock) unit.getStatements().get(1);
    checkTypes(
      loopBlock.getStatements(),
      LiteralStatement.class, WriteStatement.class, LiteralStatement.class);
    assertStatementText("b", loopBlock.getStatements().get(0));
    assertStatementText("c", loopBlock.getStatements().get(2));
  }

  public void testAliases() throws Exception {
    String templateText = "<& foo//baz &><& bar//hit/me &>";
    TemplateUnit unit = analyzeText(templateText);
    Collection<String> deps = unit.getTemplateDependencies();
    assertEquals(2, deps.size());
    assertTrue(deps.contains("/x/y/baz"));
    assertTrue(deps.contains("/z/q/hit/me"));
  }

  public void testEscapingDirective() throws Exception {
    TemplateUnit unit = analyzeText("<%escape #x><% i %>");
    WriteStatement statement = (WriteStatement) unit.getStatements().get(0);
    assertEquals(EscapingDirective.get("x"), getDefaultEscaping(statement));
  }

  public void testEscapingProperty() throws Exception {
    TemplateUnit unit = analyzeText("<% i %>");
    WriteStatement statement = (WriteStatement) unit.getStatements().get(0);
    assertEquals(EscapingDirective.get("j"), getDefaultEscaping(statement));
  }

  private Object getDefaultEscaping(WriteStatement statement) throws NoSuchFieldException,
    IllegalAccessException {
    Field field = WriteStatement.class.getDeclaredField("escapingDirective");
    field.setAccessible(true);
    Object object = field.get(statement);
    return object;
  }

  public void testForBlock() throws Exception {
    checkLoopBlock(FlowControlBlock.class, "for");
  }

  public void testWhileBlock() throws Exception {
    checkLoopBlock(FlowControlBlock.class, "while");
  }

  public void testTextCompactification() throws Exception {
    TemplateUnit unit = analyzeText("a<%def d></%def>b");
    checkTypes(unit.getStatements(), LiteralStatement.class);
    assertStatementText("ab", unit.getStatements().get(0));
  }

  public void testLiteralCompactification() throws Exception {
    TemplateUnit unit = analyzeText("a<%LITERAL>b\n</%LITERAL>\nc");
    checkTypes(unit.getStatements(), LiteralStatement.class);
    assertStatementText("ab\n\nc", unit.getStatements().get(0));
  }

  public void testAnnotations() throws Exception {
    TemplateUnit templateUnit = analyzeText("a<%annotate @Foo #impl%>\n<%annotate @Bar#proxy %>\n<%annotate @Baz%>");
    assertEquals(
      Arrays.asList(
        new AnnotationNode(location(1, 2), "@Foo ", AnnotationType.IMPL),
        new AnnotationNode(location(2, 1), "@Bar", AnnotationType.PROXY),
        new AnnotationNode(location(3, 1), "@Baz", AnnotationType.BOTH)),
      templateUnit.getAnnotations());
  }

  public void testReplacing() throws Exception {
    // TemplateDescriber will need to see the source for /foo
    TemplateUnit templateUnit =
      analyze(ImmutableMap.of(PATH, "<%replaces /foo>", "/foo", "<%replaceable>"));
    assertEquals("/foo", templateUnit.getReplacedTemplatePath());
  }

  public void testAbstractReplacesError() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(PATH, "<%abstract><%replaces /foo>"),
      new ParserErrorImpl(location(1, 12), Analyzer.ABSTRACT_REPLACING_TEMPLATE_ERROR));
  }

  public void testMissingRequiredArgsInReplacement() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(
        PATH, "<%replaces /foo><%args>int i; int j;</%args>",
        "/foo", "<%replaceable>"),
      new ParserErrorImpl(location(1, 1), "Replaced template contains no required argument named i"),
      new ParserErrorImpl(location(1, 1), "Replaced template contains no required argument named j"));
  }

  public void testMissingFragsInReplacement() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(
        PATH, "<%replaces /foo><%frag f/>",
        "/foo",  "<%replaceable>"),
      new ParserErrorImpl(location(1, 1), "Replaced template contains no fragment argument named f"));
  }

  public void testMissingOptionalArgsInReplacement() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(
        PATH, "<%replaces /foo><%args>int i = 1; int j = 2; int k = 3;</%args>",
        "/foo", "<%replaceable><%args>int j = 2; int k;</%args>"),
      new ParserErrorImpl(
        location(1, 1), "Replaced template contains no required or optional argument named i"));
  }

  public void testCircularInheritance() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(PATH, "<%extends " + PATH + ">"),
      new ParserErrorImpl(location(1, 1), "cyclic inheritance or replacement involving " + PATH));
  }

  public void testCircularReplacement() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(PATH, "<%replaces " + PATH + ">"),
      new ParserErrorImpl(location(1, 1), "cyclic inheritance or replacement involving " + PATH));
  }

  public void testAbstractReplacing() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(PATH, "<%abstract>\n<%replaces /foo>", "/foo", "<%replaceable>"),
      new ParserErrorImpl(location(2, 1), Analyzer.ABSTRACT_REPLACING_TEMPLATE_ERROR));
  }

  public void testAbstractReplaceable() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(PATH, "<%abstract>\n<%replaceable>"),
      new ParserErrorImpl(location(2, 1), Analyzer.ABSTRACT_REPLACEABLE_TEMPLATE_ERROR));
  }

  public void testReplacingNonReplaceable() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(PATH, "<%replaces /foo>", "/foo", ""),
      new ParserErrorImpl(location(1, 1), Analyzer.REPLACING_NON_REPLACEABLE_TEMPLATE_ERROR));
  }

  public void testMissingParent() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(PATH, "<%extends /foo>"),
      new ParserErrorImpl(location(1, 1), "Unable to find template or class for /foo"));

  }

  public void testMissingParentWithXargs() throws Exception {
    analyzeExpectingErrors(
      ImmutableMap.of(PATH, "<%extends /foo>\n<%xargs>x;</%xargs>"),
      new ParserErrorImpl(location(1, 1), "Unable to find template or class for /foo"),
      new ParserErrorImpl(location(2, 1), Analyzer.XARGS_DECLARED_WITHOUT_PARENT_ERROR));
  }

  private void analyzeExpectingErrors(Map<String, String> contents, ParserErrorImpl... errors)
  throws IOException {
    try {
      analyze(contents);
      fail("Exception expected");
    }
    catch (ParserErrorsImpl e) {
      assertEquals(Arrays.asList(errors), e.getErrors());
    }
  }

  private TemplateUnit analyze(Map<String, String> contents) throws IOException {
    return new Analyzer(
      PATH,
      new TemplateDescriber(
        new MockTemplateSource(contents), getClass().getClassLoader())).analyze();
  }

  private LocationImpl location(int line, int column) {
    return new LocationImpl(new TemplateResourceLocation(PATH), line, column);
  }
}
