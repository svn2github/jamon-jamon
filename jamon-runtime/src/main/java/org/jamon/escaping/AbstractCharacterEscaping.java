/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;

public abstract class AbstractCharacterEscaping implements Escaping {

  @Override
  public void write(String string, Writer writer) throws IOException {
    for (int i = 0; i < string.length(); i++) {
      write(string.charAt(i), writer);
    }
  }

  protected abstract void write(char p_char, Writer p_writer) throws IOException;

}
