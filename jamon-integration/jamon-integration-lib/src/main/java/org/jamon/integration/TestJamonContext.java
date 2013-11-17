/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.integration;

public class TestJamonContext {
  public TestJamonContext(int data) {
    this.data = data;
  }

  public int getData() {
    return data;
  }

  private final int data;
}
