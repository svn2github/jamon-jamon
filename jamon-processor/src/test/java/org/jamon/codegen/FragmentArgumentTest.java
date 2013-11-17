/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.codegen;

import static org.junit.Assert.*;

import org.junit.Test;

public class FragmentArgumentTest {
  @Test
  public void testGetFullyQualifiedTypeForTopLevelFragment() {
    FragmentArgument fragmentArgument = new FragmentArgument(
      new FragmentUnit(
        "frag",
        new TemplateUnit("foo/bar/Baz", null),
        new GenericParams(),
        null,
        null),
      null);
    assertEquals("foo.bar.Baz.Fragment_frag", fragmentArgument.getFullyQualifiedType());
  }

  @Test
  public void testGetFullyQualifiedTypeForMethodLevelFragment() {
    FragmentArgument fragmentArgument = new FragmentArgument(
      new FragmentUnit(
        "frag",
        new DeclaredMethodUnit("meth", null, null, null),
        new GenericParams(),
        null,
        null),
      null);
    assertEquals("Fragment_meth__jamon__frag", fragmentArgument.getFullyQualifiedType());
  }

}
