/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.emit;

public final class LimitedEmitter {
  private LimitedEmitter() {} // non instantiable

  public static String valueOf(int value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(double value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(char value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(boolean value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(float value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(short value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(long value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(byte value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(Integer value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(Double value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(Character value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(Boolean value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(Float value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(Short value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(Long value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(Byte value) { return StandardEmitter.valueOf(value); }
  public static String valueOf(String value) { return StrictEmitter.valueOf(value); }
}
