/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;

class OptionalValueTagEndDetector implements TagEndDetector {
  public static final String NEED_SEMI_OR_ARROW = "Expecting a ';', '=' or '=>'";

  @Override
  public int checkEnd(char character) {
    return character == ';'
        ? 1
        : 0;
  }

  @Override
  public ParserErrorImpl getEofError(Location startLocation) {
    return new ParserErrorImpl(startLocation, ArgsParser.EOF_LOOKING_FOR_SEMI);
  }

  @Override
  public void resetEndMatch() {}
}
