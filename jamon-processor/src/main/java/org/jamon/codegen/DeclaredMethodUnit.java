/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.Collection;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;

public class DeclaredMethodUnit extends AbstractInnerUnit implements MethodUnit {
  public DeclaredMethodUnit(
    String name, TemplateUnit parent,
    ParserErrorsImpl errors,
    Location location,
    boolean isAbstract) {
    super(name, parent, errors, location);
    this.isAbstract = isAbstract;
  }

  public DeclaredMethodUnit(
    String name, TemplateUnit parent, ParserErrorsImpl errors, Location location) {
    this(name, parent, errors, location, false);
  }

  @Override
  public String getOptionalArgDefaultMethod(OptionalArgument arg) {
    return "__jamon__get_Method_Opt_" + arg.getName() + "_default";
  }

  @Override
  public boolean isAbstract() {
    return isAbstract;
  }

  @Override
  public boolean doesIO() {
    return isAbstract || super.doesIO() || ((TemplateUnit) getParentUnit()).isParent();
  }

  private final boolean isAbstract;

  @Override
  public Collection<OptionalArgument> getOptionalArgsWithDefaults() {
    return getSignatureOptionalArgs();
  }

  @Override
  public String getDefaultForArg(OptionalArgument arg) {
    return arg.getDefault();
  }

  @Override
  public boolean isOverride() {
    return false;
  }
}
