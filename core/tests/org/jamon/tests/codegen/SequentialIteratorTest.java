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
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.tests.codegen;

import junit.framework.TestCase;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.jamon.codegen.SequentialIterator;

public class SequentialIteratorTest
    extends TestCase
{
    public void testNoIters()
    {
        assertEquals(constructIterator(new Object[0]),
                     new SequentialIterator(new Iterator[0]));
    }

    public void testOneEmptyIter()
    {
        Iterator i1 = constructIterator(new String[0]);
        assertEquals(i1, new SequentialIterator(new Iterator[] {i1}));
    }

    public void testOneIter()
    {
        Iterator i1 = constructIterator(new String[] {"one", "two"});
        assertEquals(i1, new SequentialIterator
            (new Iterator[] {constructIterator(new String[] {"one", "two"})}));
    }

    public void testTwoIters()
    {
        Iterator i1 = constructIterator(new String[] {"one", "two"});
        Iterator i2 = constructIterator(new String[] {"three", "four"});
        Iterator combined = constructIterator
            (new String[] {"one", "two", "three", "four"});
        assertEquals(combined, new SequentialIterator(i1, i2));
    }

    public void testTwoItersFirstEmpty()
    {
        Iterator i1 = constructIterator(new String[0]);
        Iterator i2 = constructIterator(new String[] {"three", "four"});
        Iterator combined = constructIterator(new String[] {"three", "four"});
        assertEquals(combined, new SequentialIterator(i1, i2));
    }

    public void testTwoItersSecondEmpty()
    {
        Iterator i1 = constructIterator(new String[] {"one", "two"});
        Iterator i2 = constructIterator(new String[0]);
        Iterator combined = constructIterator(new String[] {"one", "two"});
        assertEquals(combined, new SequentialIterator(i1, i2));
    }

    public void testThreeItersSecondEmpty()
    {
        Iterator i1 = constructIterator(new String[] {"one", "two"});
        Iterator i2 = constructIterator(new String[0]);
        Iterator i3 = constructIterator(new String[] {"three", "four"});
        Iterator combined = constructIterator
            (new String[] {"one", "two", "three", "four"});
        assertEquals(combined, new SequentialIterator(i1, i2, i3));
    }

    private static Iterator constructIterator(Object[] p_objs)
    {
        return Arrays.asList(p_objs).iterator();
    }

    private void assertEquals(Iterator p_expected, Iterator p_actual)
    {
        while(p_expected.hasNext())
        {
            assertTrue(p_actual.hasNext());
            assertEquals(p_expected.next(),
                         p_actual.next());
        }
        assertTrue(! p_actual.hasNext());
        try
        {
            p_actual.next();
            fail("next should have thrown an exception");
        }
        catch(NoSuchElementException e)
        {}
    }
}
