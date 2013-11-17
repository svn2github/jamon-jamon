/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.codegen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jamon.api.TemplateLocation;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.compiler.TemplateFileLocation;
import org.jamon.node.AbsolutePathNode;
import org.jamon.node.LocationImpl;
import org.jamon.node.NamedAliasPathNode;
import org.jamon.node.PathElementNode;
import org.jamon.node.RelativePathNode;
import org.jamon.node.RootAliasPathNode;
import org.jamon.node.UpdirNode;

import junit.framework.TestCase;

public class PathAdapterTest extends TestCase {
  private static final String TEMPLATE_DIR = "/templateDir";

  private static final TemplateLocation TEMPLATE_LOCATION = new TemplateFileLocation("/foobar");

  private static final org.jamon.api.Location LOCATION =
    new LocationImpl(TEMPLATE_LOCATION, 1, 1);

  private PathAdapter adapter;

  private ParserErrorsImpl errors;

  @Override
  protected void setUp() throws Exception {
    Map<String, String> aliases = new HashMap<String, String>();
    aliases.put("/", "/root/dir");
    aliases.put("foo", "/foo/dir");
    errors = new ParserErrorsImpl();
    adapter = new PathAdapter(TEMPLATE_DIR + "/", aliases, errors);
  }

  public void testAbsolutePath() throws Exception {
    new AbsolutePathNode(LOCATION)
      .addPathElement(new PathElementNode(LOCATION, "baz"))
      .apply(adapter);
    assertEquals("/baz", adapter.getPath());
  }

  public void testRelativePath() throws Exception {
    new RelativePathNode(LOCATION)
      .addPathElement(new PathElementNode(LOCATION, "baz"))
      .apply(adapter);
    assertEquals("baz", adapter.getPath());
  }

  public void testNamedAliasedPath() throws Exception {
    new NamedAliasPathNode(LOCATION, "foo")
      .addPathElement(new PathElementNode(LOCATION, "baz"))
      .apply(adapter);
    assertEquals("/foo/dir/baz", adapter.getPath());
  }

  public void testRootAliasPath() throws Exception {
    new RootAliasPathNode(LOCATION)
      .addPathElement(new PathElementNode(LOCATION, "baz"))
      .apply(adapter);
    assertEquals("/root/dir/baz", adapter.getPath());
  }

  public void testUpdirAtFrontPath() throws Exception {
    new RelativePathNode(LOCATION)
      .addPathElement(new UpdirNode(LOCATION))
      .addPathElement(new PathElementNode(LOCATION, "baz"))
      .apply(adapter);
    assertEquals("/baz", adapter.getPath());
  }

  public void testUpdirOutOfRoot() throws Exception {
    org.jamon.api.Location location2 = new LocationImpl(TEMPLATE_LOCATION, 2, 2);
    new RelativePathNode(LOCATION)
      .addPathElement(new UpdirNode(LOCATION))
      .addPathElement(new UpdirNode(location2))
      .apply(adapter);
    assertEquals(Arrays.asList(
      new ParserErrorImpl(location2, "Cannot reference templates above the root")),
      errors.getErrors());
  }

  public void testUpdirInMiddleOfPath() throws Exception {
    new RelativePathNode(LOCATION)
      .addPathElement(new PathElementNode(LOCATION, "baz"))
      .addPathElement(new UpdirNode(LOCATION))
      .addPathElement(new PathElementNode(LOCATION, "bar"))
      .apply(adapter);
    assertEquals(TEMPLATE_DIR + "/bar", adapter.getPath());
  }

  public PathAdapterTest(String name) {
    super(name);
  }

}
