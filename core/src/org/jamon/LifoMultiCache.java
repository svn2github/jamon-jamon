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

package org.jamon;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * A cache which allows multiple values for keys, and which removes
 * values after getting them.
 */

public class LifoMultiCache
{
    /**
     * Construct a <code>LifoMultiCache</code>.
     *
     * @param p_maxSize the maximum number of values in the cache
     */
    public LifoMultiCache(int p_maxSize)
    {
        // assert: p_maxSize >= 0
        m_maxSize = p_maxSize;
        m_cache = new HashMap();
        m_keyList = new LinkedList();
    }

    /**
     * Get a value from the cache corresponding to the given key. If
     * multiple values exist for the key, return the value most
     * recently {@link put()}.
     *
     * @param p_key the key to find
     *
     * @return a value corresponding to the given key, or null
     */
    public Object get(Object p_key)
    {
        LinkedList values = (LinkedList) m_cache.get(p_key);
        if (values == null || values.isEmpty())
        {
            return null;
        }
        else
        {
            m_keyList.remove(m_keyList.indexOf(p_key));
            return values.removeFirst();
        }
    }

    /**
     * Put a value into the cache corresponding to the given key. More
     * than one value may be in the cache corresponding to a key.
     *
     * @param p_key the key
     * @param p_value the value to put
     */
    public void put(Object p_key, Object p_value)
    {
        if (isFull())
        {
            bumpElement();
        }
        LinkedList values = (LinkedList) m_cache.get(p_key);
        if (values == null)
        {
            values = new LinkedList();
            m_cache.put(p_key,values);
        }
        values.addFirst(p_value);
        m_keyList.addFirst(p_key);
    }

    private boolean isFull()
    {
        return m_keyList.size() == m_maxSize;
    }

    private void bumpElement()
    {
        // assert: isFull()
        Object oldKey = m_keyList.removeLast();
        // assert: m_cache.contains(oldKey)
        LinkedList values = (LinkedList) m_cache.get(oldKey);
        // assert: values != null
        // assert: values.size() >= 1
        values.removeLast();
    }

    private final int m_maxSize;
    private final Map m_cache;
    private final LinkedList m_keyList;
}
