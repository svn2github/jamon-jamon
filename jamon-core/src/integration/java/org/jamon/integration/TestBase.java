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
 * Contributor(s): Ian Robertson
 */

package org.jamon.integration;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.jamon.TemplateManager;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.compiler.RecompilingTemplateManager;
import org.jamon.compiler.TemplateFileLocation;
import org.jamon.compiler.TemplateProcessor;
import org.jamon.node.LocationImpl;

public abstract class TestBase
    extends TestCase
{
    @Override public void setUp()
    {
        m_recompilingTemplateManager = null;
        resetWriter();
    }

    private static final String BASEDIR =
        System.getProperty("org.jamon.integration.basedir");
    protected static final String SOURCE_DIR =
        BASEDIR + File.separator + "templates";
    protected static final String WORK_DIR =
        BASEDIR + File.separator + "build/work";

    protected void resetWriter()
    {
        m_writer = new StringWriter();
    }

    protected Writer getWriter()
    {
        return m_writer;
    }

    protected void checkOutputContains(String p_expected)
    {
        assertTrue("output doesn't contain: \"" + p_expected + "\"",
                   getOutput().indexOf(p_expected) >= 0);
    }

    protected void checkOutput(String p_expected)
    {
        assertEquals(p_expected, getOutput());
    }

    protected void checkOutput(String p_message, String p_expected)
    {
        assertEquals(p_message, p_expected, getOutput());
    }

    protected TemplateManager getRecompilingTemplateManager()
    {
        if(m_recompilingTemplateManager == null)
        {
            m_recompilingTemplateManager =
                constructRecompilingTemplateManager();
        }
        return m_recompilingTemplateManager;
    }

    private TemplateManager constructRecompilingTemplateManager()
    {
        return new RecompilingTemplateManager(
            new RecompilingTemplateManager.Data()
                .setSourceDir(SOURCE_DIR)
                .setJavaCompiler(System.getProperty
                                 ("org.jamon.integration.compiler"))
                .setClasspath(System.getProperty
                              ("org.jamon.integration.classpath"))
                .setWorkDir(WORK_DIR));
    }

    private String removeCrs(CharSequence p_string)
    {
        StringBuilder buffer = new StringBuilder(p_string.length());
        for (int i = 0; i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            if (c != '\r')
            {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

    protected String getOutput()
    {
        m_writer.flush();
        return removeCrs(m_writer.getBuffer());
    }

    protected void generateSource(String p_path)
        throws Exception
    {
        String integrationDir =
            System.getProperty("org.jamon.integration.basedir");
        new TemplateProcessor(new File(integrationDir + "/build/src"),
                              new File(integrationDir + "/templates"),
                              getClass().getClassLoader())
            .generateSource(p_path);
    }

    protected static class PartialError
    {
        private final String m_message;
        private final int m_line, m_column;
        public PartialError(final String p_message, final int p_line, final int p_column)
        {
            m_message = p_message;
            m_line = p_line;
            m_column = p_column;
        }

        public ParserErrorImpl makeError(String p_path)
        {
            return new ParserErrorImpl(
                new LocationImpl(
                    new TemplateFileLocation(getTemplateFilePath(p_path)), m_line, m_column),
                m_message);
        }
    }

    protected void expectParserErrors(String p_path, PartialError... p_partialErrors)
    throws Exception
    {
        String path = "test/jamon/broken/" + p_path;
        try
        {
            generateSource(path);
            fail();
        }
        catch(ParserErrorsImpl e)
        {
            List<ParserErrorImpl> expected = new ArrayList<ParserErrorImpl>(p_partialErrors.length);
            for (PartialError partialError: p_partialErrors)
            {
                expected.add(partialError.makeError(path));
            }

            assertEquals(expected, e.getErrors());
        }

    }

    protected void expectParserError(
        String p_path, String p_message, int p_line, int p_column)
        throws Exception
    {
        expectParserErrors(p_path, new PartialError(p_message, p_line, p_column));
    }

    private static String getTemplateFilePath(String p_path)
    {
        return System.getProperty("org.jamon.integration.basedir")
            + "/templates/" + p_path + ".jamon";
    }

    public static void assertEquals(String p_first, String p_second)
    {
        if( showFullContextWhenStringEqualityFails() )
        {
            assertEquals((Object) p_first, (Object) p_second);
        }
        else
        {
            Assert.assertEquals(p_first, p_second);
        }
    }

    private static boolean showFullContextWhenStringEqualityFails()
    {
        return Boolean.valueOf
            (System.getProperty
             ("org.jamon.integration.verbose","false")).booleanValue();
    }

    private TemplateManager m_recompilingTemplateManager;
    private StringWriter m_writer;
}
