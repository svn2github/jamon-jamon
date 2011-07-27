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
import java.io.Reader;

import org.jamon.api.Location;
import org.jamon.api.TemplateLocation;
import org.jamon.codegen.AnnotationType;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbsMethodNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.AliasDefNode;
import org.jamon.node.AliasesNode;
import org.jamon.node.AnnotationNode;
import org.jamon.node.ClassNode;
import org.jamon.node.EscapeDirectiveNode;
import org.jamon.node.ExtendsNode;
import org.jamon.node.ImplementNode;
import org.jamon.node.ImplementsNode;
import org.jamon.node.ReplaceableNode;
import org.jamon.node.ReplacesNode;
import org.jamon.node.ImportsNode;
import org.jamon.node.LocationImpl;
import org.jamon.node.ParentMarkerNode;
import org.jamon.node.TopNode;

public class TopLevelParser extends AbstractBodyParser<TopNode> {
  public static final String BAD_ABSMETH_CONTENT =
    "<%absmeth> sections can only contain <%args> and <%frag> blocks";
  public static final String EXPECTING_SEMI = "Expecting ';'";
  public static final String EXPECTING_ARROW = "Expecting '=' or '=>'";
  public static final String MALFORMED_EXTENDS_TAG_ERROR = "Malformed <%extends ...> tag";
  public static final String MALFORMED_REPLACES_TAG_ERROR = "Malformed <%replaces ...> tag";
  public static final String MALFORMED_ANNOTATE_TAG_ERROR = "Malformed <%annotate...> tag";
  public static final String UNRECOGNIZED_ANNOTATION_TYPE_ERROR = "Unrecognized annotation type";
  private static final String BAD_ALIASES_CLOSE_TAG = "Malformed </%alias> tag";
  private static final String BAD_ABS_METHOD_CLOSE_TAG = "Malformed </%absmeth> tag";
  public static final String EXPECTING_IMPLEMENTS_CLOSE = "Expecting class name or </%implements>";
  public static final String EXPECTING_IMPORTS_CLOSE = "Expecting import or </%import>";

  public TopLevelParser(TemplateLocation location, Reader reader, String encoding) {
    super(new TopNode(
      new LocationImpl(location, 1, 1), encoding),
      new PositionalPushbackReader(location, reader, 2),
      new ParserErrorsImpl());
  }

  @Override
  public AbstractBodyParser<TopNode> parse() throws IOException {
    super.parse();
    if (errors.hasErrors()) {
      throw errors;
    }
    return this;
  }

  @Override
  protected void handleMethodTag(Location tagLocation) throws IOException {
    if (soakWhitespace()) {
      String name = readIdentifier(true);
      if (checkForTagClosure(tagLocation)) {
        root
            .addSubNode(new MethodParser(name, tagLocation, reader, errors).parse().getRootNode());
      }
    }
    else {
      addError(tagLocation, "malformed <%method methodName> tag");
    }
  }

  @Override
  protected void handleOverrideTag(Location tagLocation) throws IOException {
    if (soakWhitespace()) {
      String name = readIdentifier(true);
      if (checkForTagClosure(tagLocation)) {
        root.addSubNode(
          new OverrideParser(name, tagLocation, reader, errors).parse().getRootNode());
      }
    }
    else {
      addError(tagLocation, "malformed <%override methodName> tag");
    }
  }

  @Override
  protected void handleDefTag(Location tagLocation) throws IOException {
    if (soakWhitespace()) {
      String name = readIdentifier(true);
      if (checkForTagClosure(tagLocation)) {
        root.addSubNode(new DefParser(name, tagLocation, reader, errors).parse().getRootNode());
      }
    }
    else {
      addError(tagLocation, "malformed <%def defName> tag");
    }
  }

  @Override
  protected void handleClassTag(Location tagLocation) throws IOException {
    if (checkForTagClosure(tagLocation)) {
      root.addSubNode(new ClassNode(tagLocation, readUntil("</%class>", tagLocation)));
      soakWhitespace();
    }
  }

  @Override
  protected void handleExtendsTag(Location tagLocation) throws IOException {
    if (soakWhitespace()) {
      root.addSubNode(new ExtendsNode(tagLocation, parsePath()));
      soakWhitespace();
      checkForTagClosure(reader.getLocation());
      soakWhitespace();
    }
    else {
      addError(tagLocation, MALFORMED_EXTENDS_TAG_ERROR);
    }
  }

  @Override
  protected void handleReplacesTag(Location tagLocation) throws IOException {
    if (soakWhitespace()) {
      root.addSubNode(new ReplacesNode(tagLocation, parsePath()));
      soakWhitespace();
      checkForTagClosure(reader.getLocation());
      soakWhitespace();
    }
    else {
      addError(tagLocation, MALFORMED_REPLACES_TAG_ERROR);
    }
  }

  @Override
  protected void handleReplaceableTag(Location tagLocation) throws IOException {
    if (checkForTagClosure(tagLocation)) {
      root.addSubNode(new ReplaceableNode(tagLocation));
      soakWhitespace();
    }
  }

  @Override
  protected void handleImplementsTag(Location tagLocation) throws IOException {
    if (checkForTagClosure(tagLocation)) {
      ImplementsNode implementsNode = new ImplementsNode(tagLocation);
      root.addSubNode(implementsNode);
      while (true) {
        soakWhitespace();
        Location location = reader.getNextLocation();
        if (readChar('<')) {
          if (!checkToken("/%implements>")) {
            addError(location, EXPECTING_IMPLEMENTS_CLOSE);
          }
          soakWhitespace();
          return;
        }
        String className = readClassName(reader.getCurrentNodeLocation());
        if (className.length() == 0) {
          addError(location, EXPECTING_IMPLEMENTS_CLOSE);
          return;
        }
        if (!readChar(';')) {
          addError(reader.getNextLocation(), EXPECTING_SEMI);
        }
        implementsNode.addImplement(new ImplementNode(location, className));
      }
    }
  }

  @Override
  protected void handleImportTag(Location tagLocation) throws IOException {
    if (checkForTagClosure(tagLocation)) {
      ImportsNode importsNode = new ImportsNode(tagLocation);
      root.addSubNode(importsNode);
      while (true) {
        soakWhitespace();
        Location location = reader.getNextLocation();
        if (readChar('<')) {
          if (!checkToken("/%import>")) {
            addError(location, EXPECTING_IMPORTS_CLOSE);
          }
          soakWhitespace();
          return;
        }
        try {
          importsNode.addImport(new ImportParser(reader, errors).parse().getNode());
        }
        catch (ParserErrorImpl e) {
          addError(e);
          addError(reader.getLocation(), EXPECTING_IMPORTS_CLOSE);
          return;
        }
        soakWhitespace();
        if (!readChar(';')) {
          addError(reader.getNextLocation(), EXPECTING_SEMI);
        }

      }
    }
  }

  @Override
  protected void handleAliasesTag(Location tagLocation) throws IOException {
    checkForTagClosure(tagLocation);
    AliasesNode aliases = new AliasesNode(tagLocation);
    root.addSubNode(aliases);
    while (true) {
      soakWhitespace();
      reader.markNodeEnd();
      if (readChar('<')) {
        if (!checkToken("/%alias>")) {
          addError(reader.getLocation(), BAD_ALIASES_CLOSE_TAG);
        }
        soakWhitespace();
        return;
      }
      String name = readChar('/')
          ? "/"
          : readIdentifier(false);
      if (name.length() == 0) {
        addError(reader.getCurrentNodeLocation(), "Alias name expected");
        return;
      }
      soakWhitespace();
      if (readChar('=')) {
        readChar('>'); // support old-style syntax
        soakWhitespace();
        AbstractPathNode path = parsePath();
        if (path.getPathElements().isEmpty()) {
          return;
        }
        aliases.addAlias(new AliasDefNode(reader.getCurrentNodeLocation(), name, path));
        if (!readChar(';')) {
          addError(reader.getLocation(), EXPECTING_SEMI);
        }
      }
      else {
        addError(reader.getLocation(), EXPECTING_ARROW);
      }
    }
  }

  @Override
  protected void handleAbsMethodTag(Location tagLocation) throws IOException {
    if (soakWhitespace()) {
      String name = readIdentifier(true);
      checkForTagClosure(tagLocation);
      AbsMethodNode absMethodNode = new AbsMethodNode(tagLocation, name);
      root.addSubNode(absMethodNode);
      while (true) {
        soakWhitespace();
        reader.markNodeEnd();
        if (readChar('<')) {
          if (readChar('%')) {
            String tagName = readTagName();
            if ("args".equals(tagName)) {
              try {
                absMethodNode.addArgsBlock(
                  new ArgsParser(reader, errors, reader.getCurrentNodeLocation()).getArgsNode());
              }
              catch (ParserErrorImpl e) {
                addError(e);
              }
            }
            else if ("frag".equals(tagName)) {
              try {
                absMethodNode.addArgsBlock(
                  new FragmentArgsParser(reader, errors, reader.getCurrentNodeLocation())
                  .getFragmentArgsNode());
              }
              catch (ParserErrorImpl e) {
                addError(e);
              }
            }
            else {
              addError(reader.getLocation(), BAD_ABSMETH_CONTENT);
              return;
            }
          }
          else {
            if (!checkToken("/%absmeth>")) {
              addError(reader.getLocation(), BAD_ABS_METHOD_CLOSE_TAG);
            }
            soakWhitespace();
            return;
          }
        }
        else {
          addError(reader.getLocation(), BAD_ABSMETH_CONTENT);
          return;
        }
      }
    }
    else {
      addError(reader.getLocation(), "malformed <%absmeth methodName> tag");
    }
  }

  @Override
  protected void handleParentArgsNode(Location tagLocation) throws IOException {
    root.addSubNode(new ParentArgsParser(reader, errors, tagLocation).getParentArgsNode());
  }

  @Override
  protected void handleParentMarkerTag(Location tagLocation) throws IOException {
    if (checkForTagClosure(tagLocation)) {
      root.addSubNode(new ParentMarkerNode(tagLocation));
      soakWhitespace();
    }
  }

  @Override
  protected void handleEof() {
  // end of file is a fine thing at the top level
  }

  @Override
  protected void handleEscapeTag(Location tagLocation) throws IOException {
    soakWhitespace();
    if (!readChar('#')) {
      addError(reader.getNextLocation(), "Expecting '#'");
    }
    else {
      soakWhitespace();
      int c = reader.read();
      if (Character.isLetter((char) c)) {
        root.addSubNode(
          new EscapeDirectiveNode(tagLocation, new String(new char[] { (char) c })));
      }
      else {
        addError(reader.getLocation(), "Expecting a letter");
      }
      soakWhitespace();
      checkForTagClosure(tagLocation);
    }
    soakWhitespace();
  }

  @Override
  protected void handleGenericTag(Location tagLocation) throws IOException {
    root.addSubNode(new GenericsParser(reader, errors, tagLocation).getGenericsNode());
  }

  @Override
  protected void handleAnnotationTag(Location tagLocation) throws IOException {
    if (soakWhitespace()) {
      try {
        HashEndDetector detector = new HashEndDetector();
        String annotations = readJava(tagLocation, detector);
        AnnotationType annotationType;
        if (detector.endedWithHash()) {
          annotationType = readAnnotationType();
          soakWhitespace();
          if (!(readChar('%') && readChar('>'))) {
            throw new ParserErrorImpl(tagLocation, MALFORMED_ANNOTATE_TAG_ERROR);
          }
        }
        else {
          annotationType = AnnotationType.BOTH;
        }
        root.addSubNode(new AnnotationNode(tagLocation, annotations, annotationType));

      }
      catch (ParserErrorImpl e) {
        addError(e);
      }
      soakWhitespace();
    }
    else {
      addError(tagLocation, MALFORMED_ANNOTATE_TAG_ERROR);
    }
  }

  private AnnotationType readAnnotationType() throws IOException, ParserErrorImpl {
    Location location = reader.getLocation();
    if (readChar('p')) {
      if (checkToken("roxy")) {
        return AnnotationType.PROXY;
      }
    }
    else if (readChar('i')) {
      if (checkToken("mpl")) {
        return AnnotationType.IMPL;
      }
    }
    throw new ParserErrorImpl(location, UNRECOGNIZED_ANNOTATION_TYPE_ERROR);
  }

  @Override
  protected boolean isTopLevel() {
    return true;
  }
}
