/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.Collection;

public interface MethodUnit extends Unit {
  String getOptionalArgDefaultMethod(OptionalArgument arg);
  Collection<OptionalArgument> getOptionalArgsWithDefaults();
  String getDefaultForArg(OptionalArgument arg);
  boolean isAbstract();
  boolean isOverride();
  org.jamon.api.Location getLocation();
}
