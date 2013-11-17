/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.codegen;

import org.jamon.node.ArgNode;
import org.jamon.node.FragmentArgsNode;
import org.jamon.node.OptionalArgNode;

public interface StatementBlock {
  FragmentUnit getFragmentUnitIntf(String path);

  void addStatement(Statement statement);

  FragmentUnit addFragment(FragmentArgsNode node, GenericParams genericParams);

  void addRequiredArg(ArgNode node);

  void addOptionalArg(OptionalArgNode node);

  Unit getParentUnit();

  StatementBlock getParent();
}
