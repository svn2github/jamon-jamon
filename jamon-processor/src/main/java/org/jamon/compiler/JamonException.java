/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.compiler;

public abstract class JamonException extends Exception {

  private static final long serialVersionUID = 20090706L;

  public JamonException(String msg) {
    super(msg);
  }

  public JamonException(String msg, Throwable rootCause) {
    super(msg, rootCause);
  }

  public JamonException(Throwable rootCause) {
    super(rootCause);
  }

}
