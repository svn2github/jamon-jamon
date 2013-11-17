/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

public class DisallowedFragmentArgsTest extends TestBase {
  public void testFragmentInFragment() throws Exception {
    expectParserError("FragmentInFragment", "Fragments cannot have fragment arguments", 5, 3);
  }

  public void testOptionalArgInFragment() throws Exception {
    expectParserError("OptionalArgInFragment", "Fragments cannot have optional arguments", 5, 18);
  }

  public void testUnusedArgumentToFragment() throws Exception {
    expectParserError("UnusedArgumentToFragment", "Call provides unused arguments i", 2, 1);
  }
}
