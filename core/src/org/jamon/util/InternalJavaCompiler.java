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

import org.jamon.JamonRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class InternalJavaCompiler
    implements JavaCompiler
{
    private final String m_classPath;
    private final Method m_compile;
    private final Class<? extends Object> m_compilerClass;
    private Object m_compiler;

    public InternalJavaCompiler(String p_classPath)
        throws Exception
    {
        m_classPath = p_classPath;

        m_compilerClass = Class.forName("com.sun.tools.javac.Main");
        m_compiler = m_compilerClass.newInstance();
        m_compile = m_compilerClass.getMethod("compile", 
                                              (new String [0]).getClass());
        // check if we can invoke the compile method
        m_compile.invoke(m_compiler);
    }

    public String compile(String [] p_javaFiles)
    {
        String [] cmdline = new String[p_javaFiles.length + 2];
        System.arraycopy(p_javaFiles,0,cmdline,2,p_javaFiles.length);
        cmdline[0] = "-classpath";
        cmdline[1] = m_classPath;

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream pErr = new PrintStream(err);
        PrintStream oldErr = System.err;
        try
        {
            if (m_compiler == null)
            {
                m_compiler = m_compilerClass.newInstance();
            }
            System.setErr(new PrintStream(err));
            int code = ((Integer) m_compile.invoke(m_compiler,
                                                   new Object[] { cmdline }))
                        .intValue();
            pErr.close();
            return code == 0
                ? null
                : new String(err.toByteArray());
        }
        catch (IllegalAccessException e)
        {
            // this should never happen, since we checked for this
            //   in the constructor
            throw new JamonRuntimeException(e);
        }
        catch (InstantiationException e)
        {
            // this should never happen, since we checked for this
            //   in the constructor
            throw new JamonRuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            // internal compiler error, so the compiler object
            //   is not usable
            m_compiler = null;
            throw new JamonRuntimeException(e.getTargetException());
        }
        finally
        {
            System.setErr(oldErr);
        }
    }

}
