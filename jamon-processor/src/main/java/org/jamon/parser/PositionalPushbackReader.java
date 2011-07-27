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
import org.jamon.node.LocationImpl;

/**
 * A "pushback reader" which also tracks the current position in the file. Unlike
 * {@link java.io.PushbackReader}, this class allows pushing back an EOF marker as well
 *
 * @author ian
 **/

public class PositionalPushbackReader {
  private static class Position {
    void assign(Position position) {
      row = position.row;
      column = position.column;
    }

    public void nextColumn() {
      column++;
    }

    public void nextRow() {
      row++;
      column = 1;
    }

    public org.jamon.api.Location location(TemplateLocation templateLocation) {
      return new LocationImpl(templateLocation, row, column);
    }

    public boolean isLineStart() {
      return column == 1;
    }

    private int row = 1;

    private int column = 1;
  }

  /**
   * @param templateLocation The path to the resource being read.
   * @param reader The underlying reader to use
   */
  public PositionalPushbackReader(TemplateLocation templateLocation, Reader reader) {
    this(templateLocation, reader, 1);
  }

  public PositionalPushbackReader(
    TemplateLocation templateLocation, Reader reader, int pushbackBufferSize) {
    this.reader = reader;
    this.templateLocation = templateLocation;
    positions = new Position[pushbackBufferSize + 2];
    {
      for (int i = 0; i < positions.length; i++) {
        positions[i] = new Position();
      }
    }
    pushedbackChars = new int[pushbackBufferSize];
  }

  public int read() throws IOException {
    int c;
    if (pushedbackCharsPending > 0) {
      c = pushedbackChars[--pushedbackCharsPending];
    }
    else {
      c = reader.read();
    }
    for (int i = positions.length - 1; i > 0; i--) {
      positions[i].assign(positions[i - 1]);
    }

    if (c == '\n') {
      positions[0].nextRow();
    }
    else {
      positions[0].nextColumn();
    }
    return c;
  }

  public void unread(int c) throws IOException {
    if (pushedbackCharsPending >= pushedbackChars.length) {
      throw new IOException("Trying to push back characters than allowed");
    }
    pushedbackChars[pushedbackCharsPending++] = c;

    for (int i = 0; i < positions.length - 1; i++) {
      positions[i].assign(positions[i + 1]);
    }
  }

  /**
   * Get the location of the character just read.
   *
   * @return The current location (line and column numbers starting at 1)
   */
  public org.jamon.api.Location getLocation() {
    return positions[1].location(templateLocation);
  }

  /**
   * Get the location of the next character to be read (if there is one).
   *
   * @return The location of the next character
   */
  public Location getNextLocation() {
    return positions[0].location(templateLocation);
  }

  /**
   * @return True if the character just read was at the begining of a line
   */
  public boolean isLineStart() {
    return positions[1].isLineStart();
  }

  /**
   * Mark that we are just starting a node.
   **/
  public void markNodeBeginning() {
    currentNodePosition.assign(positions[1]);
  }

  /**
   * Mark that we have just finished a node
   **/
  public void markNodeEnd() {
    currentNodePosition.assign(positions[0]);
  }

  /**
   * Get the location of the current node, as set by {@link #markNodeBeginning()} or
   * {@link #markNodeEnd()}
   *
   * @return The location of the current node
   */
  public org.jamon.api.Location getCurrentNodeLocation() {
    return currentNodePosition.location(templateLocation);
  }

  private final Reader reader;
  private final TemplateLocation templateLocation;
  int pushedbackCharsPending = 0;
  final int pushedbackChars[];
  private final Position[] positions;
  private Position currentNodePosition = new Position();
}
