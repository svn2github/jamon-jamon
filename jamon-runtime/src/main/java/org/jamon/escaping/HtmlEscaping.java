/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;

public class HtmlEscaping extends AbstractCharacterEscaping {

  HtmlEscaping() {} // package scope constructor

  @Override
  protected void write(char character, Writer writer) throws IOException {
    switch (character) {
      case '<': writer.write("&lt;"); break;
      case '>': writer.write("&gt;"); break;
      case '&': writer.write("&amp;"); break;
      default: writer.write(character);
    }
  }

}
