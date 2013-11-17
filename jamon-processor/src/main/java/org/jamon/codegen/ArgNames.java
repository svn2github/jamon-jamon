/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

public interface ArgNames {
  public final static String WRITER = "jamonWriter";

  public final static String WRITER_DECL = "final " + ClassNames.WRITER + " " + WRITER;
}
