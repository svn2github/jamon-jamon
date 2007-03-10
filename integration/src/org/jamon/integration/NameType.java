package org.jamon.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jamon.annotations.Argument;
import org.jamon.codegen.AbstractArgument;
import static org.junit.Assert.*;

class NameType
{
    private String m_name, m_type;
    public NameType(String p_name, String p_type)
    {
        m_name = p_name;
        m_type = p_type;
    }

    @Override public boolean equals(Object obj)
    {
        NameType other = (NameType) obj;
        return other.m_name.equals(m_name) && other.m_type.equals(m_type);
    }

    @Override public String toString()
    {
        return "[" + m_name + ", " + m_type + "]";
    }

    @Override public int hashCode()
    {
        return m_name.hashCode() * 31 + m_type.hashCode();
    }

    public static void checkArgs(
        Iterator<? extends AbstractArgument> p_args,
        NameType... p_nameTypes)
    {
        assertEquals(Arrays.asList(p_nameTypes), argsToNameTypes(p_args));
    }

    public static void checkArgs(Argument[] p_argumentAnnotations, NameType... p_NameTypes)
    {
        List<NameType> arguments = new ArrayList<NameType>(p_argumentAnnotations.length);
        for (Argument annotation: p_argumentAnnotations)
        {
            arguments.add(new NameType(annotation.name(), annotation.type()));
        }
        assertEquals(Arrays.asList(p_NameTypes), arguments);
    }

    public static void checkArgSet(Argument[] p_argumentAnnotations, NameType... p_NameTypes)
    {
        Set<NameType> arguments = new HashSet<NameType>(p_argumentAnnotations.length);
        for (Argument annotation: p_argumentAnnotations)
        {
            arguments.add(new NameType(annotation.name(), annotation.type()));
        }
        assertEquals(new HashSet<NameType>(Arrays.asList(p_NameTypes)), arguments);
    }

    private static List<NameType> argsToNameTypes(Iterator<? extends AbstractArgument> p_args)
    {
        List<NameType> nameTypes = new LinkedList<NameType>();
        while (p_args.hasNext())
        {
            AbstractArgument argument = p_args.next();
            nameTypes.add(new NameType(argument.getName(), argument.getType()));
        }
        return nameTypes;
    }
}