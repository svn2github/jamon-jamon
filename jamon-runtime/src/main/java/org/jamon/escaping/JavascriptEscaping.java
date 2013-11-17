/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;

public class JavascriptEscaping extends AbstractCharacterEscaping {

  JavascriptEscaping() {
  // package scope constructor
  }

  @Override
  protected void write(char character, Writer writer) throws IOException {
    switch (character) {
      // Perhaps we should escape high UNICODE
      case '\'': writer.write("\\\'"); break;
      case '"': writer.write("\\\""); break;
      case '\n': writer.write("\\n"); break;
      case '\t': writer.write("\\t"); break;
      case '\b': writer.write("\\b"); break;
      case '\f': writer.write("\\f"); break;
      case '\r': writer.write("\\r"); break;
      case '\\': writer.write("\\\\"); break;
      default: writer.write(character);
    }
  }
}
