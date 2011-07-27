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

  private String defaultValue;
}
