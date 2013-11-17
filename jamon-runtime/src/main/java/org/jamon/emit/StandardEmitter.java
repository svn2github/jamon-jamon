/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.emit;

public final class StandardEmitter {
  private StandardEmitter() {} // non instantiable

  public static String valueOf(Object value) {
    return value != null ? value.toString() : "";
  }

  public static String valueOf(int value) { return String.valueOf(value);   }
  public static String valueOf(double value) { return String.valueOf(value); }
  public static String valueOf(char value) { return String.valueOf(value); }
  public static String valueOf(boolean value) { return String.valueOf(value); }
  public static String valueOf(float value) { return String.valueOf(value); }
  public static String valueOf(short value) { return String.valueOf(value); }
  public static String valueOf(long value) { return String.valueOf(value); }
  public static String valueOf(byte value) { return String.valueOf(value); }
  public static String valueOf(String value) { return StrictEmitter.valueOf(value); }
}
