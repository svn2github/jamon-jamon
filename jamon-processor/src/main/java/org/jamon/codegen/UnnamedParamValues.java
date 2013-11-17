/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.List;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;

public class UnnamedParamValues implements ParamValues {
  public UnnamedParamValues(List<String> params, Location location) {
    this.params = params;
    this.location = location;
  }

  @Override
  public void generateRequiredArgs(List<RequiredArgument> args, CodeWriter writer)
  throws ParserErrorImpl {
    if (args.size() != params.size()) {
      throw new ParserErrorImpl(
        location,
        "Call provides " + params.size() + " arguments when " + args.size() + " are expected");
    }
    for (String param : params) {
      writer.printListElement(param);
    }
  }

  @Override
  public String getOptionalArgValue(String argName) {
    return null;
  }

  @Override
  public boolean hasUnusedParams() {
    return false;
  }

  @Override
  public Iterable<String> getUnusedParams() {
    throw new IllegalStateException();
  }

  private final List<String> params;
  private final Location location;
}
