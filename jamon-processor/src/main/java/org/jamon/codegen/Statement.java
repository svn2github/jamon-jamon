/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.compiler.ParserErrorImpl;

/**
 * A <code>Statement</code> represents a generatable java statement as translated from a template.
 */
public interface Statement {
  /**
   * Generate the java source corresponding to this statement, emitting the code to the specified
   * writer.
   *
   * @param writer where to emit the java source
   * @param describer the <code>TemplateDescriber</code> to use
   * @throws ParserErrorImpl
   */
  void generateSource(CodeWriter writer, TemplateDescriber describer) throws ParserErrorImpl;
  // FIXME - determine parser errors before trying to generate source.
}
