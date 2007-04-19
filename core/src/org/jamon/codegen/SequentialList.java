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

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class SequentialList<T> extends AbstractList<T>
{
    @SuppressWarnings("unchecked") private SequentialList(List[] p_lists)
    {
        m_lists = p_lists;
        int size = 0;
        for (List<? extends T> list: p_lists)
        {
            size += list.size();
        }
        m_size = size;
    }

    public SequentialList(List<? extends T> p_list1)
    {
        this(new List[] { p_list1 });
    }

    public SequentialList(List<? extends T> p_list1, List<? extends T> p_list2)
    {
        this(new List[] { p_list1, p_list2 });
    }

    public SequentialList(
        List<? extends T> p_list1, List<? extends T> p_list2, List<? extends T> p_list3)
    {
        this(new List[] { p_list1, p_list2, p_list3 });
    }

    private final List<? extends T>[] m_lists;
    private final int m_size;

    @Override public T get(final int p_index)
    {
        if (p_index < 0)
        {
            throw new IndexOutOfBoundsException();
        }
        int index = p_index;
        for (List<? extends T> list: m_lists)
        {
            if (index >= list.size())
            {
                index -= list.size();
            }
            else
            {
                return list.get(index);
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override public int size()
    {
        return m_size;
    }

    @Override public Iterator<T> iterator()
    {
        @SuppressWarnings("unchecked") Iterator<? extends T>[] iters = new Iterator[m_lists.length];
        for (int i = 0; i < m_lists.length; i++)
        {
            iters[i] = m_lists[i].iterator();
        }
        return new SequentialIterator<T>(iters);
    }

}
