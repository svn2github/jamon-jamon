/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.compiler;

public class DisplayVersion {
  public static void main(String[] args) {
    System.out.println(DisplayVersion.class.getPackage().getSpecificationVersion());
  }

}
