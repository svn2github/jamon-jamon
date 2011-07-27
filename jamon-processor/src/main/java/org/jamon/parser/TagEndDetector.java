package org.jamon.parser;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;

/**
 * A class to detect when we are done reading a tag.
 **/
public interface TagEndDetector {
  /**
   * Check to see if we have reached the end of the current tag.
   *
   * @param character The character just read.
   * @return The number of characters comprising the end token, or 0 if the tag is not yet done.
   **/
  int checkEnd(char character);

  /**
   * Called if we reach end of file while parsing java. This will not be called if EOF is reached
   * while inside a java quote.
   *
   * @param startLocation The location of the start of this java block (used to report errors)
   * @return A ParserError detailing the problem
   **/
  ParserErrorImpl getEofError(Location startLocation);

  /**
   * Called after parsing a section which cannot be part of a tag (and which was not passed to the
   * checkEnd method). This method should reset any partial matches for an end token that may have
   * been found.
   **/
  void resetEndMatch();
}
