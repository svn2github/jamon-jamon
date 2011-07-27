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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

  private final Set<OptionalArgument> optionalArgs = new HashSet<OptionalArgument>();

  private final List<FragmentArgument> fragmentArgs = new LinkedList<FragmentArgument>();
}
