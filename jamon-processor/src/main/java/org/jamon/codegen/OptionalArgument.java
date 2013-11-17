/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.node.OptionalArgNode;
import org.jamon.util.StringUtils;

public class OptionalArgument extends AbstractArgument {
  public OptionalArgument(OptionalArgNode arg) {
    super(arg);
    defaultValue = arg.getValue().getValue();
  }

  public OptionalArgument(String name, String type, String defaultValue) {
    super(name, type, null);
    this.defaultValue = defaultValue;
  }

  public void setDefault(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String getDefault() {
    return defaultValue;
  }

  public String getIsNotDefaultName() {
    return "get" + StringUtils.capitalize(getName()) + "__IsNotDefault";
  }

  @Override
  public void generateImplDataCode(CodeWriter writer) {
    super.generateImplDataCode(writer);

    writer.println("public boolean " + getIsNotDefaultName() + "()");
    writer.openBlock();
    writer.println("return m_" + getName() + "__IsNotDefault;");
    writer.closeBlock();

    writer.println("private boolean m_" + getName() + "__IsNotDefault;");
  }

  @Override
  protected void generateImplDataSetterCode(CodeWriter writer) {
    super.generateImplDataSetterCode(writer);
    writer.println("m_" + getName() + "__IsNotDefault = true;");

  }

  @Override
  public String toString() {
    return "OptionalArg: {name => " + getName() + ", type => " + getType() + ", defualt => " + getDefault() + "}";
  }

  private String defaultValue;
}
