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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import org.jamon.util.StringUtils;
import org.jamon.api.Location;
import org.jamon.node.ArgNode;

public abstract class AbstractArgument {
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

  public org.jamon.api.Location getLocation() {
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

  protected void generateImplDataSetterCode(CodeWriter writer) {
    writer.printLocation(getLocation());
    writer.println("m_" + getName() + " = " + getName() + ";");
  }

  private final String name;

  private final String type;

  private final org.jamon.api.Location location;
}
