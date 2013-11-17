/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractArgsNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgTypeNode;
import org.jamon.node.FragmentArgsNode;

public class FragmentArgsParser extends AbstractArgsParser {
  public static final String EXPECTING_GREATER = "Expecting '>'";
  public static final String NEED_SEMI = "Expecting a ';'";
  public static final String FRAGMENT_ARGUMENT_HAS_NO_NAME = "Fragment argument has no name";

  /**
   * @param reader
   * @param errors
   * @param tagLocation
   * @throws IOException
   * @throws ParserErrorImpl
   */
  public FragmentArgsParser(
    PositionalPushbackReader reader, ParserErrorsImpl errors, Location tagLocation)
  throws IOException, ParserErrorImpl {
    super(reader, errors, tagLocation);
  }

  public FragmentArgsNode getFragmentArgsNode() {
    return fragmentArgsNode;
  }

  @Override
  protected boolean handleDefaultValue(
    AbstractArgsNode argsNode, ArgTypeNode argType, ArgNameNode argName) {
    // Fragment arguments cannot have default values
    return false;
  }

  @Override
  protected void checkArgsTagEnd() throws IOException {
    if (!checkToken("/%frag>")) {
      addError(reader.getLocation(), BAD_ARGS_CLOSE_TAG);
    }
  }

  @Override
  protected String postArgNameTokenError() {
    return NEED_SEMI;
  }

  @Override
  protected AbstractArgsNode makeArgsNode(Location tagLocation) {
    return fragmentArgsNode = new FragmentArgsNode(tagLocation, fragmentName);
  }

  @Override
  protected boolean finishOpenTag(Location tagLocation) throws IOException {
    if (!soakWhitespace()) {
      fragmentName = "";
      addError(tagLocation, FRAGMENT_ARGUMENT_HAS_NO_NAME);
    }
    else {
      try {
        fragmentName = readIdentifierOrThrow();
      }
      catch (NotAnIdentifierException e) {
        addError(tagLocation, FRAGMENT_ARGUMENT_HAS_NO_NAME);
      }
    }
    soakWhitespace();
    if (readChar('/')) {
      if (!readChar('>')) {
        addError(reader.getCurrentNodeLocation(), EXPECTING_GREATER);
      }
      else {
        fragmentArgsNode = new FragmentArgsNode(tagLocation, fragmentName);
        soakWhitespace();
      }
      return false;
    }
    return checkForTagClosure(tagLocation);
  }

  private String fragmentName;
  private FragmentArgsNode fragmentArgsNode;
}
