/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.api;

import java.io.OutputStream;

public interface SourceGenerator {
  void generateSource(OutputStream p_out) throws java.io.IOException;
}
