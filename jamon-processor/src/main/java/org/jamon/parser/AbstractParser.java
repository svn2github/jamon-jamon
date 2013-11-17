/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractPathNode;

/**
 * @author ian
 **/
public class AbstractParser {
  public static final String MALFORMED_TAG_ERROR = "Malformed tag";
  public static final String EOF_IN_JAVA_QUOTE_ERROR = "Reached end of file while inside a java quote";
  public static final String NOT_AN_IDENTIFIER_ERROR = "identifier exepected";
  public static final String BAD_JAVA_TYPE_SPECIFIER = "Bad java type specifier";
  public static final String BAD_ARGS_CLOSE_TAG = "malformed </%args> tag";
  public static final String INCOMPLETE_ARRAY_SPECIFIER_ERROR = "Expecting ']'";

  public AbstractParser(PositionalPushbackReader reader, ParserErrorsImpl errors) {
    this.reader = reader;
    this.errors = errors;
  }

  /**
   * Soak up all whitespace from the reader until non-whitespace or EOF is encountered.
   *
   * @return Whether or not any whitespace was encountered
   **/
  protected boolean soakWhitespace() throws IOException {
    int c;
    boolean whitespaceSeen = false;
    while ((c = reader.read()) >= 0 && Character.isWhitespace((char) c)) {
      whitespaceSeen = true;
    }
    reader.unread(c);
    return whitespaceSeen;
  }

  protected void addError(Location location, String message) {
    errors.addError(new ParserErrorImpl(location, message));
  }

  protected void addError(ParserErrorImpl error) {
    errors.addError(error);
  }

  protected static class NotAnIdentifierException extends Exception {

    private static final long serialVersionUID = 2006091701L;

  }

  /**
   * Reads in a java identifier.
   *
   * @return The identifier read.
   * @throws IOException
   * @throws NotAnIdentifierException if no identifier is found
   */
  protected String readIdentifierOrThrow() throws IOException, NotAnIdentifierException {
    int c;
    StringBuilder builder = new StringBuilder();
    if ((c = reader.read()) <= 0 || !Character.isJavaIdentifierStart((char) c)) {
      reader.unread(c);
      throw new NotAnIdentifierException();
    }
    else {
      builder.append((char) c);
    }

    while ((c = reader.read()) >= 0 && Character.isJavaIdentifierPart((char) c)) {
      builder.append((char) c);
    }
    reader.unread(c);
    return builder.toString();
  }

  /**
   * Read in a java identifier.
   *
   * @param addErrorIfNoneFound if true, and no identifier is found, then call
   *          {@link #addError(ParserErrorImpl)}
   * @return the identifier read, or the empty string if no identifier was found.
   * @throws IOException
   */
  protected String readIdentifier(boolean addErrorIfNoneFound) throws IOException {
    try {
      return readIdentifierOrThrow();
    }
    catch (NotAnIdentifierException e) {
      if (addErrorIfNoneFound) {
        addError(reader.getNextLocation(), NOT_AN_IDENTIFIER_ERROR);
      }
      return "";
    }
  }

  protected final PositionalPushbackReader reader;
  protected final ParserErrorsImpl errors;

  /**
   * Read a single character from the reader. If it is the expected character, return true;
   * otherwise, unread it and return false
   *
   * @param chararacter The expected character.
   * @return True if the character read was that expected.
   * @throws IOException
   */
  protected boolean readChar(char chararacter) throws IOException {
    int c;
    if ((c = reader.read()) == chararacter) {
      return true;
    }
    else {
      reader.unread(c);
      return false;
    }
  }

  /**
   * Read from the reader until encountering an end marker
   *
   * @param end The end marker for the string
   * @param startLocation The location marking the start of the block being read - only used for
   *          construcing error messages.
   **/
  protected String readUntil(String end, Location startLocation) throws IOException {
    StringBuilder buffer = new StringBuilder();
    int charsSeen = 0;
    int c = -1;
    while (charsSeen < end.length() && (c = reader.read()) >= 0) {
      if (end.charAt(charsSeen) == c) {
        charsSeen++;
      }
      else if (charsSeen > 0) {
        buffer.append(end.substring(0, charsSeen));
        charsSeen = 0;
        reader.unread(c);
      }
      else {
        buffer.append((char) c);
      }
    }
    if (c < 0) {
      addError(startLocation, eofErrorMessage(end));
    }
    return buffer.toString();
  }

  public static String eofErrorMessage(String end) {
    return "Reached end of file while looking for '" + end + "'";
  }

  protected String readJava(Location startLocation, TagEndDetector tagEndDetector) throws IOException,
    ParserErrorImpl {
    StringBuilder buffer = new StringBuilder();
    int c = -1;
    boolean inString = false;
    boolean inChar = false;
    Location quoteStart = null;
    while ((c = reader.read()) >= 0) {
      switch (c) {
        case '"':
          inString = !inChar && !inString;
          if (inString) {
            quoteStart = reader.getLocation();
            tagEndDetector.resetEndMatch();
          }
          else {
            quoteStart = null;
          }
          break;
        case '\'':
          inChar = !inString && !inChar;
          if (inChar) {
            quoteStart = reader.getLocation();
            tagEndDetector.resetEndMatch();
          }
          else {
            quoteStart = null;
          }
          break;
        case '\\':
          if (inString || inChar) {
            buffer.append((char) c);
            if ((c = reader.read()) < 0) {
              reader.unread(c);
              // pick up the EOF the next time
            }
          }
          break;
      }
      buffer.append((char) c);
      int endTokenLength;
      if (!(inString || inChar) && (endTokenLength = tagEndDetector.checkEnd((char) c)) > 0) {
        buffer.delete(buffer.length() - endTokenLength, buffer.length());
        return buffer.toString();
      }
    }
    if (quoteStart != null) {
      throw new ParserErrorImpl(quoteStart, EOF_IN_JAVA_QUOTE_ERROR);
    }
    else {
      throw tagEndDetector.getEofError(startLocation);
    }
  }

  protected boolean checkForTagClosure(Location tagLocation) throws IOException {
    if (readChar('>')) {
      return true;
    }
    else {
      addError(tagLocation, MALFORMED_TAG_ERROR);
      return false;
    }
  }

  /**
   * @param token The token or token fragment we expect to see
   * @return True if we see that token or token fragment
   **/
  protected boolean checkToken(String token) throws IOException {
    for (int i = 0; i < token.length(); i++) {
      if (!readChar(token.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  protected String readType(final Location location) throws IOException {
    try {
      return new TypeNameParser(location, reader, errors).getType();
    }
    catch (ParserErrorImpl e) {
      addError(e);
      return "";
    }
  }

  protected String readClassName(final Location p_location) throws IOException {
    try {
      return new ClassNameParser(p_location, reader, errors).getType();
    }
    catch (ParserErrorImpl e) {
      addError(e);
      return "";
    }
  }

  protected AbstractPathNode parsePath() throws IOException {
    return new PathParser(reader, errors).getPathNode();
  }

  /**
   * Determine if the next character is a particular one, and if so, read and append it to a
   * StringBuilder. Otherwise, do nothing.
   *
   * @param p_char The character being looked for
   * @param builder The StringBuilder
   * @return true if the character matched and was appended.
   * @throws IOException
   */
  protected boolean readAndAppendChar(char p_char, StringBuilder builder) throws IOException {
    if (readChar(p_char)) {
      builder.append(p_char);
      return true;
    }
    else {
      return false;
    }
  }
}
