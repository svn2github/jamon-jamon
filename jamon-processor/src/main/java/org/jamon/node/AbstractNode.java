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

package org.jamon.node;

import org.jamon.api.Location;

/**
 * The base class for nodes in the syntax tree of a parsed Jamon document.
 */
public abstract class AbstractNode {
  /**
   * @param location The location of this node
   **/

  protected AbstractNode(Location location) {
    if ((this.location = location) == null)
      throw new NullPointerException();
  }

  public final Location getLocation() {
    return location;
  }

  private final Location location;

  @Override
  public boolean equals(Object obj) {
    return obj instanceof AbstractNode && getClass().isInstance(obj)
      && location.equals(((AbstractNode) obj).location);
  }

  @Override
  public int hashCode() {
    return location.hashCode();
  }

  public abstract void apply(Analysis analysis);

  protected void propertiesToString(StringBuilder buffer) {
    buffer.append(location.toString());
  }

  @Override
  final public String toString() {
    StringBuilder buffer = new StringBuilder(getClass().getName());
    buffer.append("{");
    propertiesToString(buffer);
    buffer.append("}");
    return buffer.toString();
  }

  protected static void addProperty(StringBuilder buffer, String label, char character) {
    buffer.append(", ");
    buffer.append(label);
    buffer.append(": ");
    buffer.append(character);
  }

  protected static void addProperty(StringBuilder buffer, String label, Object obj) {
    buffer.append(", ");
    buffer.append(label);
    buffer.append(": ");
    buffer.append(obj.toString());
  }

  protected static void addPropertyList(
    StringBuilder buffer, String name, Iterable<? extends AbstractNode> properties) {
    buffer.append(", ");
    buffer.append(name);
    buffer.append(": [");
    boolean seenElement = false;
    for (AbstractNode node : properties) {
      if (seenElement) {
        buffer.append(", ");
      }
      else {
        seenElement = true;
      }
      buffer.append(node.toString());
    }
    buffer.append("]");
  }

}
