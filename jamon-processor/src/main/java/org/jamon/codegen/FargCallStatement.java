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
