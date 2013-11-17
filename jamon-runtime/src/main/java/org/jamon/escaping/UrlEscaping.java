/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;
import java.net.URLEncoder;

public class UrlEscaping implements Escaping {
  UrlEscaping() {} // package scope constructor

  @Override
  public void write(String string, Writer writer) throws IOException {
    writer.write(URLEncoder.encode(string, "UTF-8"));
  }
}
