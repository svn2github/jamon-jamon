package org.jamon.codegen;

import static org.junit.Assert.*;

import org.junit.Test;

public class ArgumentTest {
  private static final TemplateUnit TEMPLATE_UNIT =
    new TemplateUnit("org/jamon/Template", null);

  @Test
  public void testGetTypeForRequiredArg() {
    assertEquals("bar", new RequiredArgument("foo", "bar", null).getType());
  }

  @Test
  public void testGetTypeForTopLevelFragmentArg() {
    FragmentUnit fragmentUnit = new FragmentUnit(
      "frag", TEMPLATE_UNIT, new GenericParams(), null, null);
    FragmentArgument fragmentArgument = new FragmentArgument(fragmentUnit, null);
    assertEquals("Fragment_frag", fragmentArgument.getType());
  }

  @Test
  public void testGetTypeForMethodFragmentArg() {
    FragmentUnit fragmentUnit = new FragmentUnit(
      "frag",
      new DeclaredMethodUnit("method", TEMPLATE_UNIT, null, null),
      new GenericParams(),
      null,
      null);
    FragmentArgument fragmentArgument = new FragmentArgument(fragmentUnit, null);
    assertEquals("Fragment_method__jamon__frag", fragmentArgument.getType());
  }

  @Test
  public void testGetTypeForDefFragmentArg() {
    FragmentUnit fragmentUnit = new FragmentUnit(
      "frag",
      new DefUnit("def", TEMPLATE_UNIT, null, null),
      new GenericParams(),
      null,
      null);
    FragmentArgument fragmentArgument = new FragmentArgument(fragmentUnit, null);
    assertEquals("Fragment_def__jamon__frag", fragmentArgument.getType());
  }
}
