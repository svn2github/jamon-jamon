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
 * The Original Code is Jamon code, released ??.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.tests;

import junit.framework.TestCase;

import org.jamon.LifoMultiCache;

public class LifoMultiCacheTest
    extends TestCase
{
    private LifoMultiCache m_cache = new LifoMultiCache(3);

    private void put(Object p_key, Object p_value)
    {
        m_cache.put(p_key, p_value);
    }

    private Object get(Object p_key)
    {
        return m_cache.get(p_key);
    }

    public void testEmptyCache()
        throws Exception
    {
        assertEquals(null, get("A"));
    }

    public void testSuccessfulGet()
        throws Exception
    {
        put("A", "X");
        assertEquals("X", get("A"));
    }

    public void testUnsuccessfulGet()
        throws Exception
    {
        put("A", "X");
        assertEquals(null, get("B"));
    }

    public void testTwoGets()
        throws Exception
    {
        put("A", "X");
        assertEquals("X", get("A"));
        assertEquals(null, get("A"));
    }

    public void testMultiple()
        throws Exception
    {
        put("A", "Z");
        put("A", "Z");
        put("A", "Z");
        assertEquals("Z", get("A"));
        assertEquals("Z", get("A"));
        assertEquals("Z", get("A"));
        assertEquals(null, get("A"));
    }

    public void testLifo()
        throws Exception
    {
        put("A", "Z");
        put("A", "Y");
        put("A", "X");
        assertEquals("X", get("A"));
        assertEquals("Y", get("A"));
        assertEquals("Z", get("A"));
        assertEquals(null, get("A"));
    }

    public void testBump()
        throws Exception
    {
        put("A", "Z");
        put("A", "Y");
        put("A", "X");
        put("A", "W");
        put("B", "V");
        assertEquals("W", get("A"));
        assertEquals("X", get("A"));
        assertEquals(null, get("A"));
        assertEquals("V", get("B"));
        assertEquals(null, get("B"));
    }

    public void testSizeZero()
        throws Exception
    {
        LifoMultiCache cache = new LifoMultiCache(0);
        cache.put("A", "Z");
        assertEquals(null, cache.get("A"));
    }

    public void testSizeOne()
        throws Exception
    {
        LifoMultiCache cache = new LifoMultiCache(1);
        cache.put("A", "Z");
        assertEquals("Z", cache.get("A"));
        assertEquals(null, cache.get("A"));
        cache = new LifoMultiCache(1);
        cache.put("A", "Z");
        cache.put("B", "Y");
        assertEquals(null, cache.get("A"));
        assertEquals("Y", cache.get("B"));
        assertEquals(null, cache.get("B"));
    }
}
