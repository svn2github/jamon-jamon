/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jamon.annotations.Argument;
import org.jamon.codegen.AbstractArgument;
import static org.junit.Assert.*;

class NameType {
  private String name, type;

  public NameType(String name, String type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    NameType other = (NameType) obj;
    return other.name.equals(name) && other.type.equals(type);
  }

  @Override
  public String toString() {
    return "[" + name + ", " + type + "]";
  }

  @Override
  public int hashCode() {
    return name.hashCode() * 31 + type.hashCode();
  }

  public static void checkArgs(Iterable<? extends AbstractArgument> args, NameType... nameTypes) {
    assertEquals(Arrays.asList(nameTypes), argsToNameTypes(args));
  }

  public static void checkArgs(Argument[] argumentAnnotations, NameType... nameTypes) {
    List<NameType> arguments = new ArrayList<NameType>(argumentAnnotations.length);
    for (Argument annotation : argumentAnnotations) {
      arguments.add(new NameType(annotation.name(), annotation.type()));
    }
    assertEquals(Arrays.asList(nameTypes), arguments);
  }

  public static void checkArgSet(Argument[] argumentAnnotations, NameType... nameTypes) {
    Set<NameType> arguments = new HashSet<NameType>(argumentAnnotations.length);
    for (Argument annotation : argumentAnnotations) {
      arguments.add(new NameType(annotation.name(), annotation.type()));
    }
    assertEquals(new HashSet<NameType>(Arrays.asList(nameTypes)), arguments);
  }

  private static List<NameType> argsToNameTypes(Iterable<? extends AbstractArgument> args) {
    List<NameType> nameTypes = new LinkedList<NameType>();
    for (AbstractArgument argument : args) {
      nameTypes.add(new NameType(argument.getName(), argument.getType()));
    }
    return nameTypes;
  }
}
