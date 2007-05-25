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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

class Concatenation<T> extends AbstractCollection<T>
{
    private final Collection<? extends T>[] m_collections;
    private final int m_size;

    private Concatenation(Collection<? extends T>... p_collections)
    {
        m_collections = p_collections;
        int size = 0;
        for (Collection<?> collection: p_collections)
        {
            size += collection.size();
        }
        m_size = size;
    }

    @SuppressWarnings("unchecked")
    public Concatenation(Collection<? extends T> p_collection1, Collection<? extends T> p_collection2)
    {
        this(new Collection[] {p_collection1, p_collection2});
    }

    @SuppressWarnings("unchecked")
    public Concatenation(
        Collection<? extends T> p_collection1,
        Collection<? extends T> p_collection2,
        Collection<? extends T> p_collection3)
    {
        this(new Collection[] {p_collection1, p_collection2, p_collection3});
    }

    @Override public Iterator<T> iterator()
    {
        @SuppressWarnings("unchecked") Iterator<? extends T>[] iters =
            new Iterator[m_collections.length];
        for (int i = 0; i < m_collections.length; i++)
        {
            iters[i] = m_collections[i].iterator();
        }
        return new SequentialIterator<T>(iters);
    }

    @Override public int size()
    {
        return m_size;
    }

    @Override public boolean contains(Object o)
    {
        for (Collection<? extends T> collection: m_collections)
        {
            if (collection.contains(o))
            {
                return true;
            }
        }
        return false;
    }
}
