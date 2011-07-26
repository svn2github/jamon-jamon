package org.jamon.nodegen;

import static org.junit.Assert.*;

import org.junit.Test;

public class NodeMemberTest {

  @Test
  public void testInstanceName() {
    assertEquals("foo", new NodeMember("String:foo").instanceName());
    assertEquals("foos", new NodeMember("String:foo*").instanceName());
  }

  @Test
  public void testReservedInstanceName() {
    assertEquals("_implements", new NodeMember("String:implement*").instanceName());
    assertEquals("imports", new NodeMember("String:import*").instanceName());
  }

  @Test
  public void testReservedParameterName() {
    assertEquals("implement", new NodeMember("String:implement*").parameterName());
    assertEquals("_import", new NodeMember("String:import*").parameterName());
  }

}
