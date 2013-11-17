/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
