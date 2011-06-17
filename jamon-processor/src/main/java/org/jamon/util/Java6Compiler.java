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
 * The Initial Developer of the Original Code is Matt Raible.  Portions
 * created by Matt Raible are Copyright (C) 2003 Matt Raible.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.util;

import java.io.StringWriter;
import java.util.List;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Compiler that uses the Java 6 Compiler API
 * to do compilation in-memory.
 */
public class Java6Compiler
    implements JavaCompiler
{
    private final List<String> m_compilerArgs;
    private final javax.tools.JavaCompiler m_javaCompiler;
    private final StandardJavaFileManager m_javaFileManager;

    /**
     * Constructor for creating a new Java6Compiler.
     * @param p_compilerArgs the arguments to pass to the compiler.
     */
    public Java6Compiler(List<String> p_compilerArgs)
    {
        m_javaCompiler = ToolProvider.getSystemJavaCompiler();
        m_javaFileManager = m_javaCompiler.getStandardFileManager(null, null, null);

        m_compilerArgs = p_compilerArgs;
    }

    @Override
    public String compile(String [] p_javaFiles)
    {
        Iterable<? extends JavaFileObject> fileObjects =
            m_javaFileManager.getJavaFileObjects(p_javaFiles);
        StringWriter stringWriter = new StringWriter();
        if (!m_javaCompiler.getTask(
            stringWriter, m_javaFileManager, null, m_compilerArgs, null, fileObjects).call()) {
            return stringWriter.toString();
        }
        else {
            return null;
        }
    }

}
