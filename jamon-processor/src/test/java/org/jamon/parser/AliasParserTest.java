/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.parser;

import org.jamon.node.AbsolutePathNode;
import org.jamon.node.AbstractPathNode;
import org.jamon.node.AliasDefNode;
import org.jamon.node.AliasesNode;
import org.jamon.node.TopNode;
import org.junit.Test;

import static org.junit.Assert.*;

public class AliasParserTest extends AbstractParserTest {
  @Test
  public void testEmptyAliases() throws Exception {
    assertEquals(aliases(), parse("<%alias></%alias>"));
  }

  @Test
  public void testRootAlias() throws Exception {
    assertEquals(
      aliases(new AliasDefNode(location(2, 1), "/", makeAbsolutePath("/foo/bar", 2, 6))),
      parse("<%alias>\n/ => /foo/bar;\n</%alias>"));
  }

  @Test
  public void testNamedAlias() throws Exception {
    assertEquals(
      aliases(new AliasDefNode(location(2, 1), "baz", makeAbsolutePath("/foo/bar", 2, 8))),
      parse("<%alias>\nbaz => /foo/bar;\n</%alias>"));
  }

  @Test
  public void testMultipleAliases() throws Exception {
    assertEquals(
      aliases(
        new AliasDefNode(location(2, 1), "foo", makeAbsolutePath("a", 2, 8)),
        new AliasDefNode(location(3, 1), "bar", makeAbsolutePath("b", 3, 8))),
      parse("<%alias>\nfoo => /a;\nbar => /b;\n</%alias>"));
  }

  @Test
  public void testQuotedAlias() throws Exception {
    assertErrorPair("<%alias>\nhtml = \"/foo;\n</%alias>", 2, 8, PathParser.GENERIC_PATH_ERROR, 3,
      1, "Unexpected tag close </%alias>");
  }

  @Test
  public void testNonPathCharacters() throws Exception {
    assertError("<%alias>\n#", 2, 1, "Alias name expected");
  }

  private AbstractPathNode makeAbsolutePath(String path, int row, int column) {
    return buildPath(location(row, column + 1), new AbsolutePathNode(location(row, column)), path);
  }

  private static TopNode aliases(AliasDefNode... aliases) {
      AliasesNode aliasesNode = new AliasesNode(location(1,1));
      for (AliasDefNode alias: aliases) {
        aliasesNode.addAlias(alias);
      }
      return (TopNode) topNode().addSubNode(aliasesNode);
  }

  public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(AliasParserTest.class);
  }
}
