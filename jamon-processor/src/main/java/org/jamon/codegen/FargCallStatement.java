/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;

public class FargCallStatement extends AbstractCallStatement {
  FargCallStatement(
    String path,
    ParamValues params,
    FragmentUnit fragmentUnit,
    Location location,
    String templateIdentifier) {
    super(path, params, location, templateIdentifier);
    this.fragmentUnit = fragmentUnit;
  }

  private final FragmentUnit fragmentUnit;

  @Override
  public void addFragmentImpl(FragmentUnit unit, ParserErrorsImpl errors) {
    errors.addError("Fragment args for fragments not implemented", getLocation());
  }

  @Override
  protected String getFragmentIntfName(FragmentUnit fragmentUnitIntf) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void generateSource(CodeWriter writer, TemplateDescriber describer)
  throws ParserErrorImpl {
    generateSourceLine(writer);
    String tn = getPath();
    writer.print(tn + ".renderNoFlush");
    writer.openList();
    writer.printListElement(ArgNames.WRITER);
    getParams().generateRequiredArgs(fragmentUnit.getRequiredArgs(), writer);
    writer.closeList();
    writer.println(";");
    checkSuppliedParams();
  }
}
