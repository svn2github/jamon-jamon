/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.util.StringUtils;
import org.jamon.api.Location;
import org.jamon.node.ArgNode;

public abstract class AbstractArgument implements Comparable<AbstractArgument> {
  public AbstractArgument(String name, String type, Location location) {
    this.name = name;
    this.type = type;
    this.location = location;
  }

  public AbstractArgument(ArgNode arg) {
    this(arg.getName().getName(), arg.getType().getType(), arg.getLocation());
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  /**
   * Return the fully qualified type. This will always be the same as the result of
   * {@link #getType()}, except for top-level fragement arguments.
   *
   * @return the type, or for a top-level fragment argument, the fully qualified type.
   */
  public String getFullyQualifiedType() {
    return getType();
  }

  public Location getLocation() {
    return location;
  }

  public String getSetterName() {
    return "set" + StringUtils.capitalize(getName());
  }

  public String getGetterName() {
    return "get" + StringUtils.capitalize(getName());
  }

  public void generateImplDataCode(CodeWriter writer) {
    writer.printLocation(getLocation());
    writer.println("public void " + getSetterName() + "(" + getFullyQualifiedType() + " "
      + getName() + ")");
    writer.openBlock();
    generateImplDataSetterCode(writer);
    writer.closeBlock();

    writer.println("public " + getFullyQualifiedType() + " " + getGetterName() + "()");
    writer.openBlock();
    writer.println("return m_" + getName() + ";");
    writer.closeBlock();
    writer.println("private " + getFullyQualifiedType() + " m_" + getName() + ";");
  }

  @Override
  public int compareTo(AbstractArgument other) {
    int result = name.compareTo(other.name);
    if (result == 0) {
      result = type.compareTo(other.type);
    }
    return result;
  }

  protected void generateImplDataSetterCode(CodeWriter writer) {
    writer.printLocation(getLocation());
    writer.println("m_" + getName() + " = " + getName() + ";");
  }

  private final String name;

  private final String type;

  private final Location location;
}
