/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;

public class NamedParamValues implements ParamValues {
  public NamedParamValues(Map<String, String> params, Location location) {
    if (params == null) {
      this.params = Collections.emptyMap();
    }
    else {
      this.params = params;
    }
    this.location = location;
  }

  @Override
  public void generateRequiredArgs(List<RequiredArgument> args, CodeWriter writer)
  throws ParserErrorImpl {
    boolean multipleArgsAreMissing = false;
    StringBuilder missingArgs = null;
    for (RequiredArgument arg : args) {
      String name = arg.getName();
      String expr = params.remove(name);
      if (expr == null) {
        if (missingArgs == null) {
          missingArgs = new StringBuilder(name);
        }
        else {
          multipleArgsAreMissing = true;
          missingArgs.append(", " + name);
        }
      }
      writer.printListElement(expr);
    }
    if (missingArgs != null) {
      String plural = multipleArgsAreMissing
          ? "s"
          : "";
      throw new ParserErrorImpl(
        location,
        "No value" + plural + " supplied for required argument"
        + plural + " " + missingArgs.toString());
    }
  }

  @Override
  public String getOptionalArgValue(String argName) {
    return params.remove(argName);
  }

  @Override
  public boolean hasUnusedParams() {
    return !params.isEmpty();
  }

  @Override
  public Iterable<String> getUnusedParams() {
    return params.keySet();
  }

  private final Map<String, String> params;

  private final Location location;
}
