package org.jamon.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class TemplateDependenciesTest extends TestCase {

	public void testEmpty() {
		TemplateDependencies deps = new TemplateDependencies();
		assertEquals(0, deps.getDependenciesOf("a").size());
	}
	
	public void testAdd() {
		TemplateDependencies deps = new TemplateDependencies();
		final String caller = "/foo/bar";
		final String callee1 = "/b1/b2";
		final Set callees = new HashSet();
		callees.add(callee1);
		deps.setCalledBy(caller, callees);
		assertEquals(1, deps.getDependenciesOf(callee1).size());
		assertEquals(0, deps.getDependenciesOf(caller).size());
		assertTrue(deps.getDependenciesOf(callee1).contains(caller));
	}
	
	public void testBigAdd() {
		TemplateDependencies deps = new TemplateDependencies();
		final String caller = "/foo/bar";
		final String callee1 = "/b1/b2";
		final String callee2 = "/b1/b3";
		final String callee3 = "/b1/b4";
		final Set callees = new HashSet();
		callees.add(callee1);
		callees.add(callee2);
		callees.add(callee3);
		deps.setCalledBy(caller, callees);
		assertEquals(1, deps.getDependenciesOf(callee1).size());
		assertEquals(1, deps.getDependenciesOf(callee2).size());
		assertEquals(1, deps.getDependenciesOf(callee3).size());
		assertEquals(0, deps.getDependenciesOf(caller).size());
		assertTrue(deps.getDependenciesOf(callee1).contains(caller));
		assertTrue(deps.getDependenciesOf(callee2).contains(caller));
		assertTrue(deps.getDependenciesOf(callee3).contains(caller));
	}

	public void testMultipleCallers() {
		TemplateDependencies deps = new TemplateDependencies();
		final String caller1 = "/foo/bar";
		final String callee = "/b1/b2";
		final String caller2 = "/b1/b3";
		final String caller3 = "/b1/b4";
		final Set callees = new HashSet();
		callees.add(callee);

		deps.setCalledBy(caller1, callees);
		deps.setCalledBy(caller2, callees);
		deps.setCalledBy(caller3, callees);
		assertEquals(3, deps.getDependenciesOf(callee).size());
		assertEquals(0, deps.getDependenciesOf(caller1).size());
		assertEquals(0, deps.getDependenciesOf(caller2).size());
		assertEquals(0, deps.getDependenciesOf(caller3).size());
		assertTrue(deps.getDependenciesOf(callee).contains(caller1));
		assertTrue(deps.getDependenciesOf(callee).contains(caller2));
		assertTrue(deps.getDependenciesOf(callee).contains(caller3));
	}
	
	public void testRemoveDependencies() {
		TemplateDependencies deps = new TemplateDependencies();
		final String caller = "/foo/bar";
		Set callees = new HashSet();
		callees.add("a1");
		callees.add("a2");
		callees.add("a3");
		deps.setCalledBy(caller, callees);
		final String callee1 = "/b1/b2";
		final String callee2 = "/b1/b3";
		final String callee3 = "/b1/b4";
		callees = new HashSet();
		callees.add(callee1);
		callees.add(callee2);
		callees.add(callee3);
		deps.setCalledBy(caller, callees);
		assertEquals(1, deps.getDependenciesOf(callee1).size());
		assertEquals(1, deps.getDependenciesOf(callee2).size());
		assertEquals(1, deps.getDependenciesOf(callee3).size());
		assertEquals(0, deps.getDependenciesOf(caller).size());
		assertTrue(deps.getDependenciesOf(callee1).contains(caller));
		assertTrue(deps.getDependenciesOf(callee2).contains(caller));
		assertTrue(deps.getDependenciesOf(callee3).contains(caller));
	}
	
	public void testSaveAndLoad() throws Exception {
		TemplateDependencies deps = new TemplateDependencies();
		final String caller = "/foo/bar";
		Set callees = new HashSet();
		final String callee1 = "/b1/b2";
		final String callee2 = "/b1/b3";
		final String callee3 = "/b1/b4";
		callees.add(callee1);
		callees.add(callee2);
		callees.add(callee3);
		deps.setCalledBy(caller, callees);
		File tmpFile = File.createTempFile("unittest", ".dep");
		FileOutputStream out = new FileOutputStream(tmpFile);
		deps.save(out);
		out.close();
		FileInputStream in = new FileInputStream(tmpFile);
		deps = new TemplateDependencies(in);
		assertEquals(1, deps.getDependenciesOf(callee1).size());
		assertEquals(1, deps.getDependenciesOf(callee2).size());
		assertEquals(1, deps.getDependenciesOf(callee3).size());
		assertEquals(0, deps.getDependenciesOf(caller).size());
		assertTrue(deps.getDependenciesOf(callee1).contains(caller));
		assertTrue(deps.getDependenciesOf(callee2).contains(caller));
		assertTrue(deps.getDependenciesOf(callee3).contains(caller));
		in.close();
	}
}
