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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.util;

import org.jamon.JamonException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class InternalJavaCompiler
    implements JavaCompiler
{
    private final String m_classPath;
    private final Method m_compile;
    private final Class m_compilerClass;

    public InternalJavaCompiler(String p_classPath)
        throws Exception
    {
        m_classPath = p_classPath;
        m_compilerClass = Class.forName("com.sun.tools.javac.Main");
        m_compilerClass.newInstance();
        m_compile = m_compilerClass.getMethod
            ("compile", new Class [] {(new String [0]).getClass()});

        // FIXME: check for access
        // m_compile.invoke(m_compiler, new String[0]);
    }

    public void compile(String [] p_javaFiles)
        throws IOException
    {
        String [] cmdline = new String[p_javaFiles.length + 2];
        System.arraycopy(p_javaFiles,0,cmdline,2,p_javaFiles.length);
        cmdline[0] = "-classpath";
        cmdline[1] = m_classPath;

        try
        {
            Object compiler = m_compilerClass.newInstance();
            int code = ((Integer) m_compile.invoke(compiler,
                                                   new Object[] { cmdline }))
                        .intValue();
            if (code != 0)
            {
                throw new IOException("Compilation failed code=" + code);
            }
        }
        catch (IllegalAccessException e)
        {
            throw new JamonException(e);
        }
        catch (InstantiationException e)
        {
            throw new JamonException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new JamonException(e.getTargetException());
        }
    }

}
