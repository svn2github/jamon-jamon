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
 * created by Ian Robertson are Copyright (C) 2011 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.Set;

import org.jamon.compiler.RecompilingTemplateManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.common.io.Resources;


public class JavaCompilerFactoryTest
{
    private static final String JAVA_CLASS_NAME = "DummyFileToCompile";
    private static final String JAVA_FILE = JAVA_CLASS_NAME + ".java";

    private RecompilingTemplateManager.Data m_data;
    private File m_workDir;
    private ClassLoader m_workClassLoader;
    private JavaCompiler m_compiler;

    private static class FilteredClassLoader extends SecureClassLoader
    {
        private final Set<String> m_classesToReload =
            ImmutableSet.of(JavaCompilerFactory.class.getName(),
                InternalJavaCompiler.class.getName());
        public FilteredClassLoader(ClassLoader parentLoader)
        {
            super(parentLoader);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException
        {
            if (name.equals("javax.tools.JavaCompiler")) {
                throw new ClassNotFoundException();
            }
            if (m_classesToReload.contains(name)) {
                String classFileName = name.replace('.', '/') + ".class";
                URL resource = getResource(classFileName);
                byte[] classContents = null;
                try {
                    classContents =  Files.toByteArray(new File(resource.toURI()));
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Class<?> clazz = defineClass(name, classContents, 0, classContents.length);
                resolveClass(clazz);
                return clazz;
            }
            else {
                return super.loadClass(name, resolve);
            }
        }
    }

    @Before
    public void setUp() throws Exception
    {
        m_data = new RecompilingTemplateManager.Data();
        m_data.setSourceDir(
            new File(getClass().getClassLoader().getResource(JAVA_FILE).toURI()).getParent());
        m_workDir = Files.createTempDir();
        m_data.setWorkDir(m_workDir.getPath());
        m_workClassLoader = new URLClassLoader(new URL[] { m_workDir.toURI().toURL() });
    }

    @After
    public void tearDown() throws Exception
    {
        if (m_workDir != null) {
            Files.deleteRecursively(m_workDir);
        }
    }

    @Test
    public void testJava6Compiler() throws Exception
    {
        initializeJava6Compiler();
        compileAndVerifyDummyFile();
    }

    @Test
    public void testJava6CompilerWithError() throws Exception
    {
        initializeJava6Compiler();
        compileFileWithError();
    }

    @Test
    public void testJava5Compiler() throws Exception
    {
        initializeJava5Compiler();
        compileAndVerifyDummyFile();
    }

    @Test
    public void testJava5CompilerWithError() throws Exception
    {
        initializeJava5Compiler();
        compileFileWithError();
    }

    @Test
    public void testExternalJavaCompiler() throws Exception
    {
        initializeExternalJavaCompiler();
        compileAndVerifyDummyFile();
    }

    @Test
    public void testExternalJavaCompilerWithError() throws Exception
    {
        initializeExternalJavaCompiler();
        compileFileWithError();
    }

    private void initializeJava6Compiler()
    {
        m_compiler =
            JavaCompilerFactory.makeCompiler(m_data, m_workDir.getAbsolutePath(), getClass().getClassLoader());
        assertEquals(Java6Compiler.class, m_compiler.getClass());
    }

    private void initializeJava5Compiler() throws Exception
    {
        ClassLoader loader = getClass().getClassLoader();
        try {
            Class.forName("com.sun.tools.javac.Main");
        }
        catch (ClassNotFoundException e) {
            String resource = getClass().getClassLoader().getResource("java/lang/String.class").getFile();
            if (resource.startsWith("file:")) {
                String jarFile = resource.substring(5, resource.indexOf('!'));
                File toolsJar = new File(
                    new File(
                        new File(jarFile).getParentFile().getParentFile().getParentFile(),
                        "lib"),
                    "tools.jar");
                loader = new URLClassLoader(new URL[] {toolsJar.toURI().toURL()}, loader);
            }
        }
        Class<?> factoryClass = new FilteredClassLoader(loader)
            .loadClass(JavaCompilerFactory.class.getName());
        Method makeCompilerMethod = factoryClass.getDeclaredMethod(
            "makeCompiler",
            RecompilingTemplateManager.Data.class, String.class, ClassLoader.class);
        m_compiler = (JavaCompiler) makeCompilerMethod.invoke(
            null, m_data, m_workDir.getAbsolutePath(), getClass().getClassLoader());
        // due to different loaders, have to compare by name
        assertEquals(InternalJavaCompiler.class.getName(), m_compiler.getClass().getName());
    }

    private void initializeExternalJavaCompiler()
    {
        m_data.setJavaCompiler(JavaCompilerFactory.getDefaultJavac());
        m_compiler =
            JavaCompilerFactory.makeCompiler(m_data, m_workDir.getAbsolutePath(), getClass().getClassLoader());
        assertEquals(ExternalJavaCompiler.class, m_compiler.getClass());
    }

    private void compileFileWithError() throws Exception
    {
        String errors = m_compiler.compile(toJavaFileList("WithError.java"));
        assertThat(errors, JUnitMatchers.containsString("missing return statement"));
    }

    private void compileAndVerifyDummyFile() throws Exception
    {
        String errors = m_compiler.compile(toJavaFileList(JAVA_FILE));
        assertNull(errors, errors);

        Class<?> clazz = m_workClassLoader.loadClass(JAVA_CLASS_NAME);
        assertEquals("success", clazz.getDeclaredMethod("foo").invoke(null));
    }

    private String[] toJavaFileList(String javaFile) throws IOException
    {
        File destFile = new File(m_workDir, javaFile);
        Files.copy(
            Resources.newInputStreamSupplier(getClass().getClassLoader().getResource(javaFile)),
            destFile);
        return new String[] { destFile.getAbsolutePath() };
    }
}
