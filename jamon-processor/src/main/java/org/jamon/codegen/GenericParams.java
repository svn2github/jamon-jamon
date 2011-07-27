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
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import java.util.LinkedList;
import java.util.List;

import org.jamon.node.GenericsBoundNode;
import org.jamon.node.GenericsParamNode;

public class GenericParams {
  public void addParam(GenericsParamNode node) {
    genericsParamNodes.add(node);
  }

  public String generateGenericsDeclaration() {
    return generateGenericsSpecifiers(true);
  }

  public String generateGenericParamsList() {
    return generateGenericsSpecifiers(false);
  }

  public int getCount() {
    return genericsParamNodes.size();
  }

  private String generateGenericsSpecifiers(boolean forDeclaration) {
    if (genericsParamNodes.isEmpty()) {
      return "";
    }
    else {
      StringBuilder builder = new StringBuilder();
      builder.append('<');
      boolean paramsPrinted = false;
      for (GenericsParamNode genericsParamNode : genericsParamNodes) {
        if (paramsPrinted) {
          builder.append(", ");
        }
        else {
          paramsPrinted = true;
        }
        builder.append(genericsParamNode.getName());
        if (forDeclaration) {
          boolean boundsPrinted = false;
          for (GenericsBoundNode bound : genericsParamNode.getBounds()) {
            if (!boundsPrinted) {
              builder.append(" extends ");
              boundsPrinted = true;
            }
            else {
              builder.append(" & ");
            }
            builder.append(bound.getClassName());
          }
        }
      }
      builder.append('>');
      return builder.toString();
    }

  }

  private final List<GenericsParamNode> genericsParamNodes = new LinkedList<GenericsParamNode>();
}
