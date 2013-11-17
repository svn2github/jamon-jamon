/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.nodegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class NodeDescriptor {
  public NodeDescriptor(String line, Map<String, NodeDescriptor> nodes) {
    StringTokenizer tokenizer = new StringTokenizer(line, " ");
    name = tokenizer.nextToken();
    int index;
    if ((index = name.indexOf(':')) >= 0) {
      parent = name.substring(index + 1);
      parentMembers = nodes.get(parent).getAllMembers();
      name = name.substring(0, index);
    }
    else {
      parent = "AbstractNode";
      parentMembers = new ArrayList<NodeMember>();
    }

    members = new ArrayList<NodeMember>();
    while (tokenizer.hasMoreTokens()) {
      members.add(new NodeMember(tokenizer.nextToken()));
    }
  }

  public List<NodeMember> getAllMembers() {
    ArrayList<NodeMember> allMembers = new ArrayList<NodeMember>(parentMembers);
    allMembers.addAll(members);
    return allMembers;
  }

  public List<NodeMember> getMembers() {
    return members;
  }

  public String getName() {
    return name;
  }

  public List<NodeMember> getParentMembers() {
    return parentMembers;
  }

  public String getParent() {
    return parent;
  }

  private List<NodeMember> parentMembers;

  private String name;

  private List<NodeMember> members;

  private String parent;

}
