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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.jamon.JamonException;
import org.jamon.util.StringUtils;

public abstract class AbstractUnitInfo
{
    public AbstractUnitInfo(String p_name)
    {
        m_name = p_name;
    }
    private final String m_name;

    public String getName()
    {
        return m_name;
    }

    public abstract void addFarg(String p_name, String p_type);

    public void addRequiredArg(String p_name, String p_type)
    {
        m_requiredArgs.add(new Argument(p_name, p_type));
    }

    public Iterator getRequiredArgs()
    {
        return m_requiredArgs.iterator();
    }

    public boolean hasRequiredArgs()
    {
        return !m_requiredArgs.isEmpty();
    }

    public abstract void addOptionalArg(String p_name,
                                        String p_type,
                                        String p_default);

    public void printRequiredArgsDecl(IndentingWriter p_writer)
    {
        printArgsDecl(p_writer, getRequiredArgs());
    }

    protected static void printArgsDecl(IndentingWriter p_writer, Iterator i)
    {
        while(i.hasNext())
        {
            Argument arg = (Argument) i.next();
            p_writer.print("final " + arg.getType() + " " + arg.getName());
            if (i.hasNext())
            {
                p_writer.print(", ");
            }
        }
    }

    public void printRequiredArgs(IndentingWriter p_writer)
    {
        for (Iterator i = getRequiredArgs(); i.hasNext(); /* */)
        {
            p_writer.print(((Argument)i.next()).getName());
            if (i.hasNext())
            {
                p_writer.print(", ");
            }
        }
    }

    private final List m_requiredArgs = new LinkedList();
}
