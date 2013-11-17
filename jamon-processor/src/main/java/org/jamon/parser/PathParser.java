/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import java.io.IOException;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.NamedAliasPathNode;
import org.jamon.node.PathElementNode;
import org.jamon.node.RelativePathNode;
import org.jamon.node.RootAliasPathNode;
import org.jamon.node.UpdirNode;

public class PathParser extends AbstractParser {
  public static final String GENERIC_PATH_ERROR = "Malformed path";

  public PathParser(PositionalPushbackReader reader, ParserErrorsImpl errors)
      throws IOException {
    super(reader, errors);
    path = parse();
  }

  public AbstractPathNode getPathNode() {
    return path;
  }

  private AbstractPathNode parse() throws IOException {
    AbstractPathNode path;
    Location location = reader.getNextLocation();
    int c = reader.read();
    switch (c) {
      case '/':
        path = readChar('/')
            ? (AbstractPathNode) new RootAliasPathNode(location)
            : (AbstractPathNode) new AbsolutePathNode(location);
        addToPath(path, false);
        break;
      case '.':
        reader.unread(c);
        path = new RelativePathNode(location);
        addToPath(path, true);
        break;
      default:
        if (Character.isJavaIdentifierStart((char) c)) {
          reader.unread(c);
          String identifier = readIdentifier(true);
          if ((c = reader.read()) == '/') {
            if ((c = reader.read()) == '/') {
              path = new NamedAliasPathNode(location, identifier);
              addToPath(path, false);
            }
            else {
              reader.unread(c);
              path = new RelativePathNode(location).addPathElement(
                new PathElementNode(location, identifier));
              addToPath(path, false);
            }
          }
          else {
            reader.unread(c);
            path = new RelativePathNode(location).addPathElement(
              new PathElementNode(location, identifier));
          }
        }
        else {
          addError(location, GENERIC_PATH_ERROR);
          path = new RelativePathNode(location);
        }
        break;
    }
    return path;
  }

  private void addToPath(AbstractPathNode path, boolean updirsAllowedAtStart)
  throws IOException {
    int c;
    StringBuilder identifier = new StringBuilder();
    boolean identStart = true;
    boolean updirsAllowed = updirsAllowedAtStart;
    Location location = reader.getNextLocation();
    while ((c = reader.read()) >= 0) {
      if (c == '/') {
        if (identStart) {
          addError(location, GENERIC_PATH_ERROR);
          return;
        }
        else {
          path.addPathElement(new PathElementNode(location, identifier.toString()));
          identifier = new StringBuilder();
          identStart = true;
          updirsAllowed = false;
          location = reader.getNextLocation();
        }
      }
      else if (c == '.') {
        if (updirsAllowed) {
          if (reader.read() == '.') {
            path.addPathElement(new UpdirNode(location));
            identifier = new StringBuilder();
            identStart = true;
            if ((c = reader.read()) == '/') {
              location = reader.getNextLocation();
            }
            else {
              if (Character.isJavaIdentifierPart((char) c)) {
                addError(location, GENERIC_PATH_ERROR);
                return;
              }
            }
          }
          else {
            addError(location, GENERIC_PATH_ERROR);
            return;
          }
        }
        else {
          addError(location, GENERIC_PATH_ERROR);
          return;
        }
      }
      else if (identStart
          ? Character.isJavaIdentifierStart((char) c)
          : Character.isJavaIdentifierPart((char) c)) {
        identStart = false;
        identifier.append((char) c);
      }
      else {
        reader.unread(c);
        break;
      }
    }
    if (!identStart) {
      path.addPathElement(new PathElementNode(location, identifier.toString()));
    }
    else {
      addError(reader.getCurrentNodeLocation(), GENERIC_PATH_ERROR);
    }
  }

  private final AbstractPathNode path;
}
