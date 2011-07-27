/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

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
