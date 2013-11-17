/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;

public abstract class AbstractInnerUnit extends AbstractUnit {
  public AbstractInnerUnit(
    String name, StatementBlock parent, ParserErrorsImpl errors, Location location) {
    super(name, parent, errors, location);
  }

  @Override
  public void addOptionalArg(OptionalArgument arg) {
    optionalArgs.add(arg);
  }

  public Collection<OptionalArgument> getOptionalArgs() {
    return optionalArgs;
  }

  public boolean hasOptionalArgs() {
    return !optionalArgs.isEmpty();
  }

  @Override
  public void addRequiredArg(RequiredArgument arg) {
    requiredArgs.add(arg);
  }

  public Set<OptionalArgument> getOptionalArgsSet() {
    return optionalArgs;
  }

  public List<RequiredArgument> getRequiredArgs() {
    return requiredArgs;
  }

  public List<RequiredArgument> getDeclaredRequiredArgs() {
    return requiredArgs;
  }

  public boolean hasRequiredArgs() {
    return !requiredArgs.isEmpty();
  }

  @Override
  protected void addFragmentArg(FragmentArgument arg) {
    fragmentArgs.add(arg);
  }

  @Override
  public List<FragmentArgument> getFragmentArgs() {
    return fragmentArgs;
  }

  @Override
  public List<AbstractArgument> getRenderArgs() {
    return new SequentialList<AbstractArgument>(getDeclaredRequiredArgs(),
        new ArrayList<AbstractArgument>(getOptionalArgs()), getFragmentArgs());
  }

  @Override
  public Collection<AbstractArgument> getVisibleArgs() {
    return getRenderArgs();
  }

  @Override
  public List<RequiredArgument> getSignatureRequiredArgs() {
    return getRequiredArgs();
  }

  @Override
  public Collection<OptionalArgument> getSignatureOptionalArgs() {
    return getOptionalArgs();
  }

  private final List<RequiredArgument> requiredArgs = new LinkedList<RequiredArgument>();

  private final Set<OptionalArgument> optionalArgs = new TreeSet<OptionalArgument>();

  private final List<FragmentArgument> fragmentArgs = new LinkedList<FragmentArgument>();
}
