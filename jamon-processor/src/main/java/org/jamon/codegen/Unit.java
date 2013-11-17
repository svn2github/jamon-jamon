/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.Collection;
import java.util.List;

import org.jamon.compiler.ParserErrorImpl;

public interface Unit extends StatementBlock {
  String getName();
  List<FragmentArgument> getFragmentArgs();
  List<RequiredArgument> getSignatureRequiredArgs();
  Collection<OptionalArgument> getSignatureOptionalArgs();
  void printRenderArgsDecl(CodeWriter writer);

  void generateRenderBody(CodeWriter writer, TemplateDescriber describer) throws ParserErrorImpl;

  /**
   * @return {@code true} if this unit does IO, or an extension of this unit could do IO.
   */
  boolean doesIO();
}
