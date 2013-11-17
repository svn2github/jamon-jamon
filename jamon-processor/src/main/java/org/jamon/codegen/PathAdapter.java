/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.codegen;

import java.util.Map;

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.DepthFirstAnalysisAdapter;
import org.jamon.node.NamedAliasPathNode;
import org.jamon.node.PathElementNode;
import org.jamon.node.RootAliasPathNode;
import org.jamon.node.UpdirNode;

class PathAdapter extends DepthFirstAnalysisAdapter {
  public PathAdapter(String templateDir, final Map<String, String> aliases,
      ParserErrorsImpl errors) {
    this.templateDir = templateDir;
    this.aliases = aliases;
    this.errors = errors;
  }

  private final String templateDir;

  private final Map<String, String> aliases;

  private final ParserErrorsImpl errors;

  private final StringBuilder path = new StringBuilder();

  private boolean absolutePath = false;

  public String getPath() {
    return path.substring(0, path.length() - 1);
  }

  @Override
  public void inAbsolutePathNode(AbsolutePathNode node) {
    absolutePath = true;
    path.append('/');
  }

  @Override
  public void inUpdirNode(UpdirNode updir) {
    if (!absolutePath) {
      path.insert(0, templateDir);
      absolutePath = true;
    }
    int lastSlash = path.toString().lastIndexOf('/', path.length() - 2);
    if (lastSlash < 0) {
      errors.addError("Cannot reference templates above the root", updir.getLocation());
    }
    path.delete(lastSlash + 1, path.length());
  }

  @Override
  public void inPathElementNode(PathElementNode relativePath) {
    path.append(relativePath.getName());
    path.append('/');
  }

  @Override
  public void inNamedAliasPathNode(NamedAliasPathNode node) {
    String alias = aliases.get(node.getAlias());
    if (alias == null) {
      errors.addError("Unknown alias " + node.getAlias(), node.getLocation());
    }
    else {
      path.append(alias);
      path.append('/');
    }
  }

  @Override
  public void inRootAliasPathNode(RootAliasPathNode node) {
    String alias = aliases.get("/");
    if (alias == null) {
      errors.addError("No root alias", node.getLocation());
    }
    else {
      path.append(alias);
      path.append('/');
    }
  }
}
