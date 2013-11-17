/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.api.Location;

public class FragmentArgument extends RequiredArgument {
  public FragmentArgument(FragmentUnit fragmentUnit, Location location) {
    super(fragmentUnit.getName(), fragmentUnit.getFragmentInterfaceName(true), location);
    this.fragmentUnit = fragmentUnit;
  }

  public FragmentUnit getFragmentUnit() {
    return fragmentUnit;
  }

  private final FragmentUnit fragmentUnit;

  @Override
  public String getFullyQualifiedType() {
    if (getFragmentUnit().getParent() instanceof TemplateUnit) {
      String templateName = ((TemplateUnit) getFragmentUnit().getParent()).getName();
      return PathUtils.getFullyQualifiedIntfClassName(templateName) + "." + getType();
    }
    else {
      return getType();
    }
  }
}
