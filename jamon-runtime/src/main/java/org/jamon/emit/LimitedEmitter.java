/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

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
