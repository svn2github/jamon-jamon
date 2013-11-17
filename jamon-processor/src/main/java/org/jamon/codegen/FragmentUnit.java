/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.OptionalArgNode;

public class FragmentUnit extends AbstractInnerUnit {
  static final String DEFAULT_FRAGMENT_NAME = "";

  public FragmentUnit(
    String name,
    StatementBlock parent,
    GenericParams genericParams,
    ParserErrorsImpl errors,
    Location location) {
    super(name, parent, errors, location);
    this.genericParams = genericParams;
  }

  public String getFragmentInterfaceName(boolean makeGeneric) {
    String genericParamsClause = makeGeneric
        ? genericParams.generateGenericParamsList()
        : "";
    if (getParent() instanceof AbstractInnerUnit) {
      return "Fragment_" + getParentUnit().getName() + "__jamon__" + getName()
        + genericParamsClause;
    }
    else {
      return "Fragment_" + getName() + genericParamsClause;
    }
  }

  @Override
  public void addOptionalArg(OptionalArgNode node) {
    getErrors().addError("Fragments cannot have optional arguments", node.getValue().getLocation());
  }

  @Override
  public void addOptionalArg(OptionalArgument arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void addFragmentArg(FragmentArgument arg) {
    getErrors().addError("Fragments cannot have fragment arguments", arg.getLocation());
  }

  @Override
  public FragmentUnit getFragmentUnitIntf(String path) {
    return getParent().getFragmentUnitIntf(path);
  }

  public void printInterface(CodeWriter writer, String interfaceModifiers, boolean isCopy) {
    writer.println(interfaceModifiers + " static interface " + getFragmentInterfaceName(true));
    if (isCopy) {
      writer.println("  extends Intf." + getFragmentInterfaceName(true));
    }
    writer.openBlock();
    if (!isCopy) {
      writer.print("void renderNoFlush");
      writer.openList();
      writer.printListElement(ArgNames.WRITER_DECL);
      printRenderArgsDecl(writer);
      writer.closeList();
      writer.println();
      writer.println("  throws java.io.IOException;");
      writer.print(ClassNames.RENDERER + " makeRenderer");
      writer.openList();
      printRenderArgsDecl(writer);
      writer.closeList();
      writer.println(";");
    }
    writer.closeBlock();
    writer.println();
  }

  public void generateThrowsIOExceptionIfNecessary(CodeWriter writer) {
    if (doesIO()) {
      writer.println(" throws " + ClassNames.IOEXCEPTION);
    }
  }

  private final GenericParams genericParams;
}
