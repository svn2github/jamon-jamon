/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractBodyNode;
import org.jamon.node.ElseIfNode;
import org.jamon.node.ElseNode;

public class IfParser extends AbstractFlowControlBlockParser<AbstractBodyNode> {
  public static final String ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG =
    "encountered multiple <%else> tags for one <%if ...%> tag";

  public IfParser(AbstractBodyNode node, PositionalPushbackReader reader, ParserErrorsImpl errors) {
    super(node, reader, errors);
  }

  @Override
  protected void handleElseTag(Location tagLocation) throws IOException {
    if (processingElseNode()) {
      addError(tagLocation, ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG);
    }
    else {
      if (checkForTagClosure(tagLocation)) {
        continuation = new IfParser(new ElseNode(tagLocation), reader, errors);
        continuation.parse();
      }
      doneParsing();
    }
  }

  @Override
  protected void handleElseIfTag(Location tagLocation) throws IOException {
    if (processingElseNode()) {
      addError(tagLocation, ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG);
    }
    else {
      try {
        continuation = new IfParser(
          new ElseIfNode(tagLocation, readCondition(tagLocation, "elseif")),
          reader,
          errors);
        continuation.parse();
      }
      catch (ParserErrorImpl e) {
        addError(e);
      }
      doneParsing();
    }
  }

  private boolean processingElseNode() {
    return root instanceof ElseNode;
  }

  public IfParser getContinuation() {
    return continuation;
  }

  private IfParser continuation;

  @Override
  protected String tagName() {
    return "if";
  }
}
