/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;

public class NoneEscaping implements Escaping {

  NoneEscaping() {} // package scope constructor

  @Override
  public void write(String string, Writer writer) throws IOException {
    writer.write(string);
  }

}
