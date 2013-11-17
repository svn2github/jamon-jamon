/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.integration;

import java.io.IOException;
import java.io.Writer;

public interface SomeInterface {
  // FIXME: we want covariant return types

  // SomeInterface setX(int x)
  // throws IOException;

  void render(Writer w, String s) throws IOException;
}
