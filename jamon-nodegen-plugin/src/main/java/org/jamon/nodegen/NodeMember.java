package org.jamon.nodegen;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

class NodeMember {
  NodeMember(String descriptor) {
    int index = descriptor.indexOf(':');
    type = descriptor.substring(0, index);
    if (descriptor.endsWith("*")) {
      name = descriptor.substring(index + 1, descriptor.length() - 1);
      isList = true;
    }
    else {
      name = descriptor.substring(index + 1);
      isList = false;
    }
  }

  public String instanceName() {
    return escapeReservedWords(isList ? name + 's' : name);
  }

  public String parameterName() {
    return escapeReservedWords(name);
  }

  private String escapeReservedWords(String word) {
    if ("import".equals(word) || "implements".equals(word)) {
      return "_" + word;
    }
    else {
      return word;
    }
  }

  public boolean isPrimative() {
    return "char".equals(type);
  }

  public String getGetter() {
    return isList
        ? "get" + getCapitalizedName() + "s()"
        : "get" + getCapitalizedName() + "()";
  }

  public String getCapitalizedName() {
    return name.substring(0, 1).toUpperCase(Locale.US) + name.substring(1);
  }

  public String hashCodeExpr() {
    if ("char".equals(type)) {
      return instanceName();
    }
    else {
      return instanceName() + ".hashCode()";
    }
  }

  public boolean isList() {
    return isList;
  }

  public boolean isNode() {
    return !isPrimative() && !NON_NODE_TYPES.contains(type);
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  private final String type;

  private final String name;

  private final boolean isList;

  private final static Set<String> NON_NODE_TYPES = Collections
      .unmodifiableSet(new HashSet<String>(Arrays.asList("String",
        "org.jamon.codegen.AnnotationType")));
}
