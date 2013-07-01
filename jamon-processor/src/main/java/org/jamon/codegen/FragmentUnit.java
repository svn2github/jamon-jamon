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
