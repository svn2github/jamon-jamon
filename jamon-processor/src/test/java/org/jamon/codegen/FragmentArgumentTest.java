package org.jamon.codegen;

import static org.junit.Assert.*;

import org.junit.Test;

public class FragmentArgumentTest
{

    @Test
    public void testGetFullyQualifiedTypeForTopLevelFragment()
    {
        FragmentArgument fragmentArgument = new FragmentArgument(
            new FragmentUnit(
                "frag",
                new TemplateUnit("foo/bar/Baz", null),
                new GenericParams(),
                null,
                null),
            null);
        assertEquals("Fragment_frag", fragmentArgument.getType());
    }

    @Test
    public void testGetFullyQualifiedTypeForMethodLevelFragment()
    {
        FragmentArgument fragmentArgument = new FragmentArgument(
            new FragmentUnit(
                "frag",
                new DeclaredMethodUnit("meth", null, null, null),
                new GenericParams(),
                null,
                null),
            null);
        assertEquals("Fragment_meth__jamon__frag", fragmentArgument.getType());
    }

}
