/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * A simple abstract class which lacks only a Writer to which to render.
 */
public abstract class AbstractRenderer implements Renderer {
  /**
   * Render to the given writer.
   *
   * @param writer the Writer to which to render
   * @exception IOException if writing to the Writer throws an IOException
   */
  @Override
  public abstract void renderTo(Writer writer) throws IOException;

  /**
   * Render this Renderer into a String.
   *
   * @return a String that is the result of rendering this Renderer
   */
  @Override
  public final String asString() {
    StringWriter writer = new StringWriter();
    try {
      renderTo(writer);
    }
    catch (IOException e) {
      // It's a StringWriter, so we shouldn't ever get here
      throw new RuntimeException(e);
    }
    return writer.toString();
  }
}
