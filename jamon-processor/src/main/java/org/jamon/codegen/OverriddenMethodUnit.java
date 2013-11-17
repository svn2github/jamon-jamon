/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.Collection;
import java.util.List;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.ParentArgNode;

public class OverriddenMethodUnit extends AbstractUnit implements MethodUnit, InheritedUnit {
  public OverriddenMethodUnit(
    DeclaredMethodUnit declaredMethodUnit,
    Unit parent,
    ParserErrorsImpl errors,
    Location location) {
    super(declaredMethodUnit.getName(), parent, errors, location);
    this.declaredMethodUnit = declaredMethodUnit;
    inheritedArgs = new InheritedArgs(
      getName(),
      declaredMethodUnit.getRequiredArgs(),
      declaredMethodUnit.getOptionalArgsSet(),
      declaredMethodUnit.getFragmentArgs(),
      errors);

  }

  @Override
  public void addParentArg(ParentArgNode node) {
    inheritedArgs.addParentArg(node);
  }

  @Override
  public Collection<AbstractArgument> getVisibleArgs() {
    return inheritedArgs.getVisibleArgs();
  }

  private final DeclaredMethodUnit declaredMethodUnit;

  private final InheritedArgs inheritedArgs;

  @Override
  public List<FragmentArgument> getFragmentArgs() {
    return declaredMethodUnit.getFragmentArgs();
  }

  @Override
  public List<RequiredArgument> getSignatureRequiredArgs() {
    return declaredMethodUnit.getSignatureRequiredArgs();
  }

  @Override
  public Collection<OptionalArgument> getSignatureOptionalArgs() {
    return declaredMethodUnit.getSignatureOptionalArgs();
  }

  @Override
  public String getOptionalArgDefaultMethod(OptionalArgument arg) {
    return declaredMethodUnit.getOptionalArgDefaultMethod(arg);
  }

  @Override
  public void printRenderArgsDecl(CodeWriter writer) {
    for (AbstractArgument arg : declaredMethodUnit.getRenderArgs()) {
      writer.printListElement(
        "final " + arg.getType() + " "
        + (inheritedArgs.isArgVisible(arg) ? "" : "p__jamon__") + arg.getName());
    }
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  @Override
  public void addFragmentArg(org.jamon.codegen.FragmentArgument arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addRequiredArg(org.jamon.codegen.RequiredArgument arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addOptionalArg(org.jamon.codegen.OptionalArgument arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<OptionalArgument> getOptionalArgsWithDefaults() {
    return inheritedArgs.getOptionalArgsWithNewDefaultValues();
  }

  @Override
  public String getDefaultForArg(OptionalArgument arg) {
    return inheritedArgs.getDefaultValue(arg);
  }

  @Override
  public boolean isOverride() {
    return true;
  }
}
