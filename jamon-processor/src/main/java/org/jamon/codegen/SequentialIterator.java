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

package org.jamon.codegen;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SequentialIterator<T>
    implements Iterator<T>
{
    public SequentialIterator(Iterator<? extends T>[] p_iters)
    {
        m_iters = p_iters.clone();
    }

    @SuppressWarnings("unchecked")
    public SequentialIterator(Iterator<? extends T> p_iter1, 
                              Iterator<? extends T> p_iter2)
    {
        this(new Iterator[] {p_iter1, p_iter2});
    }

    @SuppressWarnings("unchecked")
    public SequentialIterator(Iterator<? extends T> p_iter1,
                              Iterator<? extends T> p_iter2,
                              Iterator<? extends T> p_iter3)
    {
        this(new Iterator[] {p_iter1, p_iter2, p_iter3});
    }

    @SuppressWarnings("unchecked")
    public SequentialIterator(Iterator<? extends T> p_iter1,
                              Iterator<? extends T> p_iter2,
                              Iterator<? extends T> p_iter3,
                              Iterator<? extends T> p_iter4)
    {
        this(new Iterator[] {p_iter1, p_iter2, p_iter3, p_iter4});
    }

    private final Iterator<? extends T>[] m_iters;
    private int currentIter = 0;

    public boolean hasNext()
    {
        if(currentIter >= m_iters.length)
        {
            return false;
        }
        else if(m_iters[currentIter].hasNext())
        {
            return true;
        }
        else
        {
            currentIter++;
            return hasNext();
        }
    }

    public T next()
        throws NoSuchElementException
    {
        if(hasNext())
        {
            return m_iters[currentIter].next();
        }
        else
        {
            throw new NoSuchElementException();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
