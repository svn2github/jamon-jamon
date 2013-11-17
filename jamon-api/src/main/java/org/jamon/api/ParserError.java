/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.api;

/**
 * An error encountered during the parsing of a Jamon template.
 */
public interface ParserError {

  /**
   * @return The location of the error
   */
  Location getLocation();

  /**
   * @return The error message
   */
  String getMessage();

}
