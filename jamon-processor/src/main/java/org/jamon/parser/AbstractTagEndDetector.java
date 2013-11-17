/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;

class AbstractTagEndDetector implements TagEndDetector {
  private final String endTag;
  private final int endTagLength;

  int charsSeen = 0;

  protected AbstractTagEndDetector(String endTag) {
    this.endTag = endTag;
    endTagLength = endTag.length();
  }

  @Override
  public int checkEnd(final char character) {
    if (character == endTag.charAt(charsSeen)) {
      if (++charsSeen == endTagLength) {
        return charsSeen;
      }
    }
    else {
      charsSeen = 0;
    }
    return 0;
  }

  @Override
  public ParserErrorImpl getEofError(Location startLocation) {
    return new ParserErrorImpl(
      startLocation, "Reached end of file while looking for '" + endTag + "'");
  }

  @Override
  public void resetEndMatch() {
    charsSeen = 0;
  }

}
