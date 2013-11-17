/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.emit;

import java.util.Locale;

public enum EmitMode {
  STANDARD(StandardEmitter.class), LIMITED(LimitedEmitter.class), STRICT(StrictEmitter.class);

  public static EmitMode fromString(String string) {
    return valueOf(string.toUpperCase(Locale.US));
  }

  public String getEmitterClassName() {
    return emitterClass.getName();
  }

  @Override
  public String toString() {
    return "EmitMode{" + emitterClass.getName() + "}";
  }

  private EmitMode(Class<?> emitterClass) {
    this.emitterClass = emitterClass;
  }

  private final Class<?> emitterClass;
}
