/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

import java.io.Writer;
import java.io.IOException;

/**
 * A simple interface describing that which knows how to render.
 */
public interface Renderer {
  /**
   * Render to the given writer.
   *
   * @param writer the Writer to which to render
   * @exception IOException if writing to the Writer throws an IOException
   */
  void renderTo(Writer writer) throws IOException;

  /**
   * Render this Renderer into a String.
   *
   * @return a String that is the result of rendering this Renderer
   */
  String asString();

}
