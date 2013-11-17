/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.List;

import org.jamon.compiler.ParserErrorImpl;

public interface ParamValues {
  void generateRequiredArgs(List<RequiredArgument> args, CodeWriter writer) throws ParserErrorImpl;

  String getOptionalArgValue(String argName);

  boolean hasUnusedParams();

  Iterable<String> getUnusedParams();
}
