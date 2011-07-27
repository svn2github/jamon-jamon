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
