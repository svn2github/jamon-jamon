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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.ArgNode;
import org.jamon.node.FragmentArgsNode;
import org.jamon.node.OptionalArgNode;

public abstract class AbstractUnit extends AbstractStatementBlock implements Unit, Comparable<AbstractUnit> {
  public AbstractUnit(
    String name, StatementBlock parent, ParserErrorsImpl errors, Location location) {
    super(parent, location);
    if (name == null) {
      throw new NullPointerException();
    }
    this.name = name;
    this.errors = errors;
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  public final Unit getParentUnit() {
    return (Unit) getParent();
  }

  protected final ParserErrorsImpl getErrors() {
    return errors;
  }

  @Override
  public int compareTo(AbstractUnit o) {
    return getName().compareTo(o.getName());
  }

  protected abstract void addFragmentArg(FragmentArgument arg);

  @Override
  public abstract List<FragmentArgument> getFragmentArgs();

  @Override
  public FragmentUnit getFragmentUnitIntf(String path) {
    for (FragmentArgument arg : getFragmentArgs()) {
      if (path.equals(arg.getName())) {
        return arg.getFragmentUnit();
      }
    }
    return null;
  }

  @Override
  public void generateRenderBody(CodeWriter writer, TemplateDescriber describer)
  throws ParserErrorImpl {
    writer.openBlock();
    printStatements(writer, describer);
    writer.closeBlock();
  }

  public abstract void addRequiredArg(RequiredArgument arg);

  public abstract void addOptionalArg(OptionalArgument arg);

  @Override
  public abstract List<RequiredArgument> getSignatureRequiredArgs();

  @Override
  public abstract Collection<OptionalArgument> getSignatureOptionalArgs();

  public abstract Collection<AbstractArgument> getVisibleArgs();

  private final String name;

  private final ParserErrorsImpl errors;

  private final Set<String> argNames = new HashSet<String>();

  @Override
  public FragmentUnit addFragment(FragmentArgsNode node, GenericParams genericParams) {
    checkArgName(node.getFragmentName(), node.getLocation());
    FragmentUnit frag = new FragmentUnit(node.getFragmentName(), this, genericParams, errors,
        node.getLocation());
    addFragmentArg(new FragmentArgument(frag, node.getLocation()));
    return frag;
  }

  @Override
  public void addRequiredArg(ArgNode node) {
    checkArgName(node.getName().getName(), node.getName().getLocation());
    addRequiredArg(new RequiredArgument(node));
  }

  @Override
  public void addOptionalArg(OptionalArgNode node) {
    checkArgName(node.getName().getName(), node.getName().getLocation());
    addOptionalArg(new OptionalArgument(node));
  }

  protected void addArgName(AbstractArgument arg) {
    argNames.add(arg.getName());
  }

  private void checkArgName(String name, org.jamon.api.Location location) {
    if (!argNames.add(name)) {
      getErrors().addError("multiple arguments named " + name, location);
    }
  }

  public List<AbstractArgument> getRenderArgs() {
    return new SequentialList<AbstractArgument>(getSignatureRequiredArgs(), getFragmentArgs());
  }

  @Override
  public void printRenderArgsDecl(CodeWriter writer) {
    printArgsDecl(writer, getRenderArgs());
  }

  public void printRenderArgs(CodeWriter writer) {
    printArgs(writer, getRenderArgs());
  }

  protected static void printArgsDecl(
    CodeWriter writer, Iterable<? extends AbstractArgument> i) {
    for (AbstractArgument arg : i) {
      writer.printListElement("final " + arg.getFullyQualifiedType() + " " + arg.getName());
    }
  }

  protected static void printArgs(CodeWriter writer, Iterable<? extends AbstractArgument> args) {
    for (AbstractArgument arg : args) {
      writer.printListElement(arg.getName());
    }
  }

  protected void generateInterfaceSummary(StringBuilder buf) {
    buf.append("Required\n");
    for (AbstractArgument arg : getSignatureRequiredArgs()) {
      buf.append(arg.getName());
      buf.append(":");
      buf.append(arg.getType());
      buf.append("\n");
    }
    buf.append("Optional\n");
    TreeMap<String, OptionalArgument> optArgs = new TreeMap<String, OptionalArgument>();
    for (OptionalArgument arg : getSignatureOptionalArgs()) {
      optArgs.put(arg.getName(), arg);
    }
    for (OptionalArgument arg : optArgs.values()) {
      buf.append(arg.getName());
      buf.append(":");
      buf.append(arg.getType());
      buf.append("\n");
    }
  }
}
