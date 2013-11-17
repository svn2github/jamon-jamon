/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.ArgValueNode;
import org.jamon.node.ParentArgNode;
import org.jamon.node.ParentArgWithDefaultNode;

public class InheritedArgs {
  public InheritedArgs(
    String parentName,
    Collection<RequiredArgument> requiredArgs,
    Collection<OptionalArgument> optionalArgs,
    Collection<FragmentArgument> fragmentArgs,
    ParserErrorsImpl errors) {
    this.parentName = parentName;
    this.errors = errors;
    this.requiredArgs = requiredArgs;
    this.optionalArgs = optionalArgs;
    this.fragmentArgs = fragmentArgs;
  }

  private final String parentName;

  private final ParserErrorsImpl errors;

  private final Set<AbstractArgument> visibleArgs = new TreeSet<AbstractArgument>();

  private final Collection<RequiredArgument> requiredArgs;

  private final Collection<OptionalArgument> optionalArgs;

  private final Collection<FragmentArgument> fragmentArgs;

  private final Map<OptionalArgument, String> defaultOverrides =
    new TreeMap<OptionalArgument, String>();

  public Collection<AbstractArgument> getVisibleArgs() {
    return visibleArgs;
  }

  public boolean isArgVisible(AbstractArgument arg) {
    return visibleArgs.contains(arg);
  }

  public void addParentArg(ParentArgNode node) {
    String name = node.getName().getName();
    ArgValueNode value = (node instanceof ParentArgWithDefaultNode)
        ? ((ParentArgWithDefaultNode) node).getValue()
        : null;
    for (RequiredArgument arg : requiredArgs) {
      if (arg.getName().equals(name)) {
        if (value == null) {
          visibleArgs.add(arg);
        }
        else {
          errors.addError(name
            + " is an inherited required argument, and may not be given a default value", value
              .getLocation());
        }
        return;
      }
    }
    for (OptionalArgument arg : optionalArgs) {
      if (arg.getName().equals(name)) {
        if (value != null) {
          defaultOverrides.put(arg, value.getValue().trim());
        }
        visibleArgs.add(arg);
        return;
      }
    }
    for (FragmentArgument arg : fragmentArgs) {
      if (arg.getName().equals(name)) {
        if (value == null) {
          visibleArgs.add(arg);
        }
        else {
          errors.addError(name
            + " is an inherited fragment argument, and may not be given a default value", value
              .getLocation());
        }
        return;
      }
    }
    errors.addError(parentName + " does not have an arg named " + name, node.getName()
        .getLocation());
  }

  public String getDefaultValue(OptionalArgument arg) {
    return defaultOverrides.get(arg);
  }

  public Collection<OptionalArgument> getOptionalArgsWithNewDefaultValues() {
    return defaultOverrides.keySet();
  }
}
