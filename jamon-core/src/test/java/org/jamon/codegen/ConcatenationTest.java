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
 * created by Ian Robertson are Copyright (C) 2007 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;


public class ConcatenationTest
{
    @Test public void twoEmpyCollections() {
        assertCollectionsEqual(new Concatenation<Integer>(make(), make()));
    }

    @Test public void twoCollections() {
        assertCollectionsEqual(new Concatenation<Integer>(make(1, 2), make(3, 4)), 1, 2, 3, 4);
    }

    @Test public void threeCollections() {
        assertCollectionsEqual(
            new Concatenation<Integer>(make(1, 2), make(5, 6), make(3, 4)),
            1, 2, 3, 4, 5, 6);
    }

    private static List<Integer> make(Integer... p_ints)
    {
        return Arrays.asList(p_ints);
    }


    private static void assertCollectionsEqual(Collection<Integer> p_actual, int... p_expected)
    {
        assertEquals(p_expected.length, p_actual.size());
        assertFalse(p_actual.contains(-1));
        Set<Integer> expected = new HashSet<Integer>();
        for (int num: p_expected)
        {
            expected.add(num);
            assertTrue(p_actual.contains(num));
        }
        assertEquals(expected, new HashSet<Integer>(p_actual));
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(ConcatenationTest.class);
    }
}
