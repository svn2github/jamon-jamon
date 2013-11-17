/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;

public class StrictHtmlEscaping extends HtmlEscaping {

  StrictHtmlEscaping() {} // package scope constructor

  @Override
  protected void write(char character, Writer writer) throws IOException {
    switch (character) {
      case '"': writer.write("&quot;"); break;
      case '\'': writer.write("&#39;"); break;
      // FIXME: numerically escape other chars
      default: super.write(character, writer);
    }
  }

}
