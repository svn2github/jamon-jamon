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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class InternalJavaCompiler
    implements JavaCompiler
{
    private final List<String> m_compilerArgs;
    private final Method m_compile;
    private final Class<? extends Object> m_compilerClass;
    private Object m_compiler;

    public InternalJavaCompiler(List<String> p_compilerArgs)
        throws Exception
    {
        m_compilerArgs = p_compilerArgs;
        m_compilerClass = getClass().getClassLoader().loadClass("com.sun.tools.javac.Main");
        m_compiler = m_compilerClass.newInstance();
        m_compile = m_compilerClass.getMethod("compile",
                                              (new String [0]).getClass());
        verifyCompiler();
    }

    private void verifyCompiler() throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException
    {
        // check if we can invoke the compile method, but redirect output
        Method compileWithWriter =
            m_compilerClass.getMethod("compile", String[].class, PrintWriter.class);
        compileWithWriter.invoke(
            m_compiler,
            new String[] { "-version" },
            new PrintWriter(new StringWriter()));
    }

    @Override
    public String compile(String [] p_javaFiles)
    {
        String [] cmdline = new String[p_javaFiles.length + m_compilerArgs.size()];
        m_compilerArgs.toArray(cmdline);
        System.arraycopy(p_javaFiles, 0, cmdline, m_compilerArgs.size(), p_javaFiles.length);

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
            throw new RuntimeException(e);
        }
        catch (InstantiationException e)
        {
            // this should never happen, since we checked for this
            //   in the constructor
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            // internal compiler error, so the compiler object
            //   is not usable
            m_compiler = null;
            throw new RuntimeException(e.getTargetException());
        }
        finally
        {
            System.setErr(oldErr);
        }
    }

}
