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
