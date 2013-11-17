/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
