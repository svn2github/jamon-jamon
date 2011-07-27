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

public abstract class AbstractInnerUnitCallStatement extends AbstractCallStatement {
  AbstractInnerUnitCallStatement(
    String path,
    ParamValues params,
    Unit unit,
    Location location,
    String templateIdentifier) {
    super(path, params, location, templateIdentifier);
    this.unit = unit;
  }

  private final Unit unit;

  protected Unit getUnit() {
    return unit;
  }

  @Override
  protected String getFragmentIntfName(FragmentUnit fragmentUnitIntf) {
    return fragmentUnitIntf.getFragmentInterfaceName(true);
  }

  @Override
  public void generateSource(CodeWriter writer, TemplateDescriber describer)
  throws ParserErrorImpl {
    generateSourceLine(writer);
    writer.openBlock();
    makeFragmentImplClasses(unit.getFragmentArgs(), writer, describer);
    generateSourceLine(writer);
    writer.print("__jamon_innerUnit__" + getPath());
    writer.openList();
    writer.printListElement(ArgNames.WRITER);
    // FIXME - do we need to surround args with parens?
    getParams().generateRequiredArgs(unit.getSignatureRequiredArgs(), writer);
    for (OptionalArgument arg : unit.getSignatureOptionalArgs()) {
      String name = arg.getName();
      String expr = getParams().getOptionalArgValue(name);
      writer.printListElement(expr == null
          ? getDefault(arg)
          : expr);
    }
    generateFragmentParams(writer, unit.getFragmentArgs());
    writer.closeList();
    writer.println(";");
    checkSuppliedParams();
    writer.closeBlock();
  }

  protected abstract String getDefault(OptionalArgument arg);
}
