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
import java.util.ArrayList;
import java.util.List;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractBodyNode;
import org.jamon.node.DefaultEscapeNode;
import org.jamon.node.DocNode;
import org.jamon.node.EmitNode;
import org.jamon.node.EscapeNode;
import org.jamon.node.ForNode;
import org.jamon.node.IfNode;
import org.jamon.node.JavaNode;
import org.jamon.node.LiteralNode;
import org.jamon.node.TextNode;
import org.jamon.node.WhileNode;

/**
 * @author ian
 **/

public abstract class AbstractBodyParser<Node extends AbstractBodyNode> extends AbstractParser {
  public static final String ENCOUNTERED_ELSE_TAG_WITHOUT_PRIOR_IF_TAG =
    "encountered <%else> tag without prior <%if ...%> tag";
  public static final String ENCOUNTERED_ELSEIF_TAG_WITHOUT_PRIOR_IF_TAG =
    "encountered <%elseif ...%> tag without prior <%if ...%> tag";
  public static final String ESCAPE_TAG_IN_SUBCOMPONENT =
    "<%escape> tags only allowed at the top level of a document";
  public static final String GENERIC_TAG_IN_SUBCOMPONENT =
    "<%generic> tags only allowed at the top level of a document";
  public static final String ANNOTATE_TAG_IN_SUBCOMPONENT =
    "<%annotate> tags only allowed at the top level of a document";
  public static final String CLASS_TAG_IN_SUBCOMPONENT =
    "<%class> sections only allowed at the top level of a document";
  public static final String UNEXPECTED_NAMED_FRAGMENT_CLOSE_ERROR =
    "</|> tags can only be used to close named call fragments";
  public static final String UNEXPECTED_FRAGMENTS_CLOSE_ERROR =
    "</&> tags can only be used to close call fragments";
  public static final String EMIT_ESCAPE_CODE_ERROR =
    "Emit escaping code must be a letter";
  public static final String EMIT_MISSING_TAG_END_ERROR =
    "Did not see expected '%>' to end a <% ... %> tag";
  public static final String PERCENT_GREATER_THAN_EOF_ERROR =
    "Reached end of file while looking for '%>'";
  public static final String EXTENDS_TAG_IN_SUBCOMPONENT =
    "<%extends ...> tag only allowed at the top level of a document";
  private static final String ALIASES_TAG_IN_SUBCOMPONENT =
    "<%aliases> sections only allowed at the top level of a document";
  public static final String IMPLEMENTS_TAG_IN_SUBCOMPONENT =
    "<%implements> sections only allowed at the top level of a document";
  public static final String REPLACES_TAG_IN_SUBCOMPONENT =
    "<%replaces ...> tag only allowed at the top level of a document";
  public static final String REPLACEABLE_TAG_IN_SUBCOMPONENT =
    "<%replaceable ...> tag only allowed at the top level of a document";
  private static final String IMPORT_TAG_IN_SUBCOMPONENT =
    "<%import> sections only allowed at the top level of a document";
  public static final String PARENT_ARGS_TAG_IN_SUBCOMPONENT =
    "<%xargs> sections only allowed at the top level of a document";
  public static final String PARENT_MARKER_TAG_IN_SUBCOMPONENT =
    "<%abstract> tag only allowed at the top level of a document";

  protected AbstractBodyParser(
    Node rootNode, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(reader, errors);
    root = rootNode;
    bodyStart = reader.getNextLocation();
  }

  protected void handleText() {
    if (text.length() > 0) {
      root.addSubNode(new TextNode(reader.getCurrentNodeLocation(), text.toString()));
      text = new StringBuilder();
    }
    reader.markNodeBeginning();
  }

  public AbstractBodyParser<Node> parse() throws IOException {
    int c;
    doneParsing = false;
    reader.markNodeEnd();
    boolean isTopLevel = isTopLevel();
    while ((isTopLevel || !doneParsing) && (c = reader.read()) >= 0) {
      if (c == '<') {
        Location tagLocation = reader.getLocation();
        int c1 = reader.read();
        switch (c1) {
          case '%': // a tag, emit or java snippet
            handleText();
            if (soakWhitespace()) {
              handleEmit(tagLocation);
            }
            else {
              handleTag(readTagName(), tagLocation);
            }
            reader.markNodeEnd();
            break;
          case '&':
            handleText();
            root.addSubNode(new CallParser(reader, errors, tagLocation).getCallNode());
            reader.markNodeEnd();
            break;
          case '/':
            switch (c = reader.read()) {
              case '%':
                String tagName = readTagName();
                doneParsing();
                if (checkForTagClosure(tagLocation)) {
                  handleTagClose(tagName, tagLocation);
                }
                break;
              case '&':
                if (readChar('>')) {
                  if (handleFragmentsClose(tagLocation)) {
                    doneParsing();
                  }
                }
                else {
                  text.append("</&");
                }
                break;
              case '|':
                if (readChar('>')) {
                  if (handleNamedFragmentClose(tagLocation)) {
                    doneParsing();
                  }
                }
                else {
                  text.append("</|");
                }
                break;
              default:
                reader.unread(c);
                text.append("</");
            }
            break;
          default:
            if (c1 >= 0) {
              reader.unread(c1);
            }
            text.append((char) c);
            break;
        }
      }
      else if (c == '%' && reader.isLineStart()) {
        handleText();
        root.addSubNode(new JavaNode(reader.getCurrentNodeLocation(), readLine()));
        reader.markNodeEnd();
      }
      else if (c == '\\') {
        List<Integer> toPushBack = new ArrayList<Integer>();
        int c1 = reader.read();
        toPushBack.add(c1);
        if (c1 == '\r') {
          c1 = reader.read();
          toPushBack.add(0, c1);
        }
        if (c1 != '\n') {
          text.append((char) c);
          for (int c2 : toPushBack) {
            reader.unread(c2);
          }
        }
      }
      else {
        text.append((char) c);
      }
    }
    handleText();
    if (!(doneParsing || isTopLevel)) {
      handleEof();
    }
    return this;
  }

  protected void doneParsing() {
    doneParsing = true;
  }

  /**
   * @param tagLocation Start of the emit.
   **/
  private void handleEmit(Location tagLocation) throws IOException {
    try {
      HashEndDetector endDetector = new HashEndDetector();
      String emitExpr = readJava(tagLocation, endDetector);
      if (!endDetector.endedWithHash()) {
        root.addSubNode(
          new EmitNode(tagLocation, emitExpr, new DefaultEscapeNode(reader.getLocation())));
      }
      else {
        Location escapingLocation = reader.getLocation();
        int c = reader.read();
        if (isLetter((char) c)) {
          soakWhitespace();
          if (readChar('%') && readChar('>')) {
            root.addSubNode(new EmitNode(
              tagLocation,
              emitExpr,
              new EscapeNode(escapingLocation, Character.toString((char) c))));
          }
          else {
            addError(reader.getLocation(), EMIT_MISSING_TAG_END_ERROR);
          }
        }
        else {
          addError(reader.getLocation(), EMIT_ESCAPE_CODE_ERROR);
        }
      }
    }
    catch (ParserErrorImpl e) {
      addError(e);
    }
  }

  private boolean isLetter(char character) {
    return ('A' <= character && character <= 'Z') || ('a' <= character && character <= 'z');
  }

  /**
   * @return <code>true</code> if this is a top level parser
   **/
  protected boolean isTopLevel() {
    return false;
  }

  protected void handleTag(final String tagName, final Location tagLocation) throws IOException {
    if ("java".equals(tagName)) {
      handleJavaTag(tagLocation);
    }
    else if ("LITERAL".equals(tagName)) {
      handleLiteralTag(tagLocation);
    }
    else if ("def".equals(tagName)) {
      handleDefTag(tagLocation);
    }
    else if ("method".equals(tagName)) {
      handleMethodTag(tagLocation);
    }
    else if ("override".equals(tagName)) {
      handleOverrideTag(tagLocation);
    }
    else if ("while".equals(tagName)) {
      handleWhileTag(tagLocation);
    }
    else if ("for".equals(tagName)) {
      handleForTag(tagLocation);
    }
    else if ("if".equals(tagName)) {
      handleIfTag(tagLocation);
    }
    else if ("else".equals(tagName)) {
      handleElseTag(tagLocation);
    }
    else if ("elseif".equals(tagName)) {
      handleElseIfTag(tagLocation);
    }
    else if ("args".equals(tagName)) {
      // FIXME - either all handlers should throw these, or none.
      try {
        root.addSubNode(new ArgsParser(reader, errors, tagLocation).getArgsNode());
      }
      catch (ParserErrorImpl e) {
        addError(e);
      }
    }
    else if ("frag".equals(tagName)) {
      try {
        root.addSubNode(new FragmentArgsParser(reader, errors, tagLocation)
            .getFragmentArgsNode());
      }
      catch (ParserErrorImpl e) {
        addError(e);
      }
    }
    else if ("xargs".equals(tagName)) {
      handleParentArgsNode(tagLocation);
    }
    else if ("class".equals(tagName)) {
      handleClassTag(tagLocation);
    }
    else if ("extends".equals(tagName)) {
      handleExtendsTag(tagLocation);
    }
    else if ("alias".equals(tagName)) {
      handleAliasesTag(tagLocation);
    }
    else if ("absmeth".equals(tagName)) {
      handleAbsMethodTag(tagLocation);
    }
    else if ("implements".equals(tagName)) {
      handleImplementsTag(tagLocation);
    }
    else if ("replaces".equals(tagName)) {
      handleReplacesTag(tagLocation);
    }
    else if ("replaceable".equals(tagName)) {
      handleReplaceableTag(tagLocation);
    }
    else if ("import".equals(tagName)) {
      handleImportTag(tagLocation);
    }
    else if ("doc".equals(tagName)) {
      handleDocTag(tagLocation);
    }
    else if ("abstract".equals(tagName)) {
      handleParentMarkerTag(tagLocation);
    }
    else if ("escape".equals(tagName)) {
      handleEscapeTag(tagLocation);
    }
    else if ("generic".equals(tagName)) {
      handleGenericTag(tagLocation);
    }
    else if ("annotate".equals(tagName)) {
      handleAnnotationTag(tagLocation);
    }
    else {
      if (checkForTagClosure(tagLocation)) {
        addError(tagLocation, "Unknown tag <%" + tagName + ">");
      }
    }
  }

  /**
   * Handle a tag closure
   *
   * @param tagName The tag name
   * @param tagLocation The tag location
   * @throws IOException
   */
  protected void handleTagClose(final String tagName, final Location tagLocation) throws IOException {
    addError(tagLocation, "Unexpected tag close </%" + tagName + ">");
  }

  /**
   * This method is called when an end of file is reached, and should add an error if this is not
   * acceptable
   **/
  abstract protected void handleEof();

  /**
   * Handle the occurence of a '&lt;/|;&gt;' tag
   *
   * @return <code>true</code> if this parser is done
   * @throws IOException
   **/
  protected boolean handleNamedFragmentClose(Location tagLocation) throws IOException {
    addError(tagLocation, UNEXPECTED_NAMED_FRAGMENT_CLOSE_ERROR);
    return false;
  }

  /**
   * Handle the occurence of a '&lt;/&amp;&gt;' tag
   *
   * @return <code>true</code> if this parser is done
   * @throws IOException
   **/
  protected boolean handleFragmentsClose(Location tagLocation) throws IOException {
    addError(tagLocation, UNEXPECTED_FRAGMENTS_CLOSE_ERROR);
    return false;
  }

  /**
   * @param tagLocation location of the {@code def} tag
   * @throws IOException
   */
  protected void handleMethodTag(Location tagLocation) throws IOException {
    addError(tagLocation, "<%method> sections only allowed at the top level of a document");
  }

  /**
   * @param tagLocation location of the def tag
   * @throws IOException
   */
  protected void handleOverrideTag(Location tagLocation) throws IOException {
    addError(tagLocation, "<%override> sections only allowed at the top level of a document");
  }

  /**
   * @param tagLocation location of the def tag
   * @throws IOException
   */
  protected void handleDefTag(Location tagLocation) throws IOException {
    addError(tagLocation, "<%def> sections only allowed at the top level of a document");
  }

  /**
   * @param tagLocation location of the absmeth tag
   * @throws IOException
   */
  protected void handleAbsMethodTag(Location tagLocation) throws IOException {
    addError(tagLocation, "<%absmeth> sections only allowed at the top level of a document");
  }

  private static class ConditionEndDetector implements TagEndDetector {
    public ConditionEndDetector(String tagName) {
      this.tagName = tagName;
    }

    @Override
    public int checkEnd(char character) {
      switch (character) {
        case '%':
          seenPercent = true;
          return 0;
        case '>':
          if (seenPercent) {
            return 2;
          }
          else {
            seenPercent = false;
            return 0;
          }
        default:
          seenPercent = false;
          return 0;
      }
    }

    @Override
    public ParserErrorImpl getEofError(Location startLocation) {
      return new ParserErrorImpl(
        startLocation, "Reached end of file while reading " + tagName + " tag");
    }

    @Override
    public void resetEndMatch() {}

    private boolean seenPercent = false;

    private final String tagName;
  }

  protected void handleWhileTag(Location tagLocation) throws IOException {
    try {
      root.addSubNode(
        new WhileParser(new WhileNode(
          tagLocation, readCondition(tagLocation, "while")), reader, errors)
        .parse().getRootNode());
    }
    catch (ParserErrorImpl e) {
      addError(e);
    }
  }

  protected void handleForTag(Location tagLocation) throws IOException {
    try {
      root.addSubNode(
        new ForParser(
          new ForNode(tagLocation, readCondition(tagLocation, "for")), reader, errors)
        .parse().getRootNode());
    }
    catch (ParserErrorImpl e) {
      addError(e);
    }
  }

  protected void handleIfTag(Location tagLocation) throws IOException {
    try {
      IfParser parser = new IfParser(
        new IfNode(tagLocation, readCondition(tagLocation, "if")), reader, errors);
      parser.parse();
      for (; parser != null; parser = parser.getContinuation()) {
        root.addSubNode(parser.getRootNode());
      }
    }
    catch (ParserErrorImpl e) {
      addError(e);
    }
  }

  protected String readCondition(Location tagLocation, String tagName)
  throws IOException, ParserErrorImpl {
    if (!soakWhitespace()) {
      throw new ParserErrorImpl(tagLocation, "Malformed <%" + tagName + " ...%> tag");
    }
    else {
      return readJava(tagLocation, new ConditionEndDetector("<%" + tagName + " ...%>"));
    }
  }

  /**
   * @param tagLocation location of the else tag
   * @throws IOException
   */
  protected void handleElseTag(Location tagLocation) throws IOException {
    addError(tagLocation, ENCOUNTERED_ELSE_TAG_WITHOUT_PRIOR_IF_TAG);
  }

  /**
   * @param tagLocation location of the {@code elseif} tag
   * @throws IOException
   */
  protected void handleElseIfTag(Location tagLocation) throws IOException {
    addError(tagLocation, ENCOUNTERED_ELSEIF_TAG_WITHOUT_PRIOR_IF_TAG);
  }

  /**
   * @param tagLocation location of the {@code xargs} node
   * @throws IOException
   */
  protected void handleParentArgsNode(Location tagLocation) throws IOException {
    addError(tagLocation, PARENT_ARGS_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code abstract} tag
   * @throws IOException
   */
  protected void handleParentMarkerTag(Location tagLocation) throws IOException {
    addError(tagLocation, PARENT_MARKER_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code escape} tag
   * @throws IOException
   */
  protected void handleEscapeTag(Location tagLocation) throws IOException {
    addError(tagLocation, ESCAPE_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code generic} tag
   * @throws IOException
   */
  protected void handleGenericTag(Location tagLocation) throws IOException {
    addError(tagLocation, GENERIC_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code annotate} tag
   * @throws IOException
   */
  protected void handleAnnotationTag(Location tagLocation) throws IOException {
    addError(tagLocation, ANNOTATE_TAG_IN_SUBCOMPONENT);
  }

  private void handleJavaTag(final Location tagLocation) throws IOException {
    if (readChar('>')) {
      handleJavaCode(tagLocation, new JavaTagEndDetector());
    }
    else {
      soakWhitespace();
      handleJavaCode(tagLocation, new JavaSnippetTagEndDetector());
    }
  }

  private void handleJavaCode(final Location tagLocation, TagEndDetector endTagDetector)
  throws IOException {
    try {
      root.addSubNode(new JavaNode(tagLocation, readJava(tagLocation, endTagDetector)));
      soakWhitespace();
    }
    catch (ParserErrorImpl e) {
      addError(e);
    }
  }

  private static class JavaTagEndDetector extends AbstractTagEndDetector {
    public JavaTagEndDetector() {
      super("</%java>");
    }

  }

  protected static class JavaSnippetTagEndDetector extends AbstractTagEndDetector {
    protected JavaSnippetTagEndDetector() {
      super("%>");
    }

  }

  protected void handleLiteralTag(final Location tagLocation) throws IOException {
    if (checkForTagClosure(tagLocation)) {
      root.addSubNode(new LiteralNode(tagLocation, readUntil("</%LITERAL>", tagLocation)));
    }
  }

  /**
   * @param tagLocation location of the {@code class} tag.
   * @throws IOException
   */
  protected void handleClassTag(Location tagLocation) throws IOException {
    addError(tagLocation, CLASS_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code extends} tag
   * @throws IOException
   */
  protected void handleExtendsTag(Location tagLocation) throws IOException {
    addError(tagLocation, EXTENDS_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code implements} tag
   * @throws IOException
   */
  protected void handleImplementsTag(Location tagLocation) throws IOException {
    addError(tagLocation, IMPLEMENTS_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code replaces} tag
   * @throws IOException
   */
  protected void handleReplacesTag(Location tagLocation) throws IOException {
    addError(tagLocation, REPLACES_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code replaceable} tag
   * @throws IOException
   */
  protected void handleReplaceableTag(Location tagLocation) throws IOException {
    addError(tagLocation, REPLACEABLE_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code import} tag
   * @throws IOException
   */
  protected void handleImportTag(Location tagLocation) throws IOException {
    addError(tagLocation, IMPORT_TAG_IN_SUBCOMPONENT);
  }

  /**
   * @param tagLocation location of the {@code alias} tag
   * @throws IOException
   */
  protected void handleAliasesTag(Location tagLocation) throws IOException {
    addError(tagLocation, ALIASES_TAG_IN_SUBCOMPONENT);
  }

  private void handleDocTag(Location tagLocation) throws IOException {
    if (checkForTagClosure(tagLocation)) {
      root.addSubNode(new DocNode(tagLocation, readUntil("</%doc>", tagLocation)));
    }
    soakWhitespace();
  }

  protected String readTagName() throws IOException {
    StringBuilder buffer = new StringBuilder();
    int c;
    while ((c = reader.read()) >= 0 && !Character.isWhitespace((char) c) && c != '>') {
      buffer.append((char) c);
    }
    if (c >= 0) {
      reader.unread(c);
    }
    return buffer.toString();
  }

  protected String readLine() throws IOException {
    int c;
    StringBuilder line = new StringBuilder();
    while ((c = reader.read()) >= 0) {
      line.append((char) c);
      if (c == '\n') {
        break;
      }
    }
    return line.toString();
  }

  public Node getRootNode() {
    return root;
  }

  protected StringBuilder text = new StringBuilder();
  protected final Node root;
  protected final Location bodyStart;
  private boolean doneParsing;
}
