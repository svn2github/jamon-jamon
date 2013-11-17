/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

public class IllegalAliasTest extends TestBase {
  public void testCircularAlias() throws Exception {
    expectParserError("CircularAlias", "Unknown alias bar", 2, 9);
  }

  public void testUnknownAlias() throws Exception {
    expectParserError("UnknownAlias", "Unknown alias foo", 4, 4);
  }

  public void testDuplicateAlias() throws Exception {
    expectParserError("DuplicateAlias", "Duplicate alias for foo", 3, 3);
  }

  public void testDuplicateAliasFromProperties() throws Exception {
    expectParserErrors(
      "aliasProperties/AliasVsProperties",
      new PartialError("Duplicate alias for jamon", 2, 3),
      new PartialError("Duplicate alias for /", 3, 3));
  }
}
