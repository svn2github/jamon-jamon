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
