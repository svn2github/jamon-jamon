/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.api;

/**
 * A location in a Jamon template.
 */
public interface Location {
  /**
   * @return the location of the template.
   */
  TemplateLocation getTemplateLocation();

  /**
   * @return the column of the location (1-based).
   */
  int getColumn();

  /**
   * @return the row of the location (1-based).
   */
  int getLine();
}