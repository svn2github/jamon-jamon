/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
