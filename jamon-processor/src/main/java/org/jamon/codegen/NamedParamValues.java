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
