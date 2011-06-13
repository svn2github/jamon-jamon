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

package org.jamon.integration;

import test.jamon.Recompilation;
import test.jamon.replacement.Api;
import test.jamon.replacement.ApiReplacement;

import org.jamon.AbstractTemplateProxy;
import org.jamon.AbstractTemplateReplacer;
import org.jamon.TemplateManager;
import org.jamon.compiler.RecompilingTemplateManager;
import org.jamon.util.StringUtils;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

public class RecompilationTest
    extends TestBase
{
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        clearWorkDir();
    }

    public void testBasicRecompilation() throws Exception
    {
        File template = templateFile(Recompilation.class);

        template.delete();

        new Recompilation(getRecompilingTemplateManager()).render(getWriter());
        checkOutput("This is the template");

        final String STRING = "This is changed\n";
        writeTemplate(template, STRING);

        resetWriter();

        new Recompilation(getRecompilingTemplateManager()).render(getWriter());
        checkOutput(STRING);
    }

    public void testReplacement() throws Exception {
        File template = templateFile(ApiReplacement.class);
        template.delete();
        template.getParentFile().mkdirs();

        TemplateManager templateManager = new RecompilingTemplateManager(
            new RecompilingTemplateManager.Data()
                .setSourceDir(SOURCE_DIR)
                .setWorkDir(WORK_DIR)
                .setTemplateReplacer(new AbstractTemplateReplacer() {
                    @Override
                    protected Class<? extends AbstractTemplateProxy> findReplacement(
                        Class<?> p_proxyClass, Object p_jamonContext)
                    {
                        if (Api.class.equals(p_proxyClass))
                        {
                            return ApiReplacement.class;
                        }
                        else
                        {
                            return null;
                        }
                    }
                }));
        new Api(templateManager).render(getWriter(), 2, 3);
        checkOutput("Replacement: 2 3");

        resetWriter();
        writeTemplate(template, "<%replaces Api><%args>int i; int j;</%args>Wow: <% i %> <% j %>");
        new Api(templateManager).render(getWriter(), 2, 3);
        checkOutput("Wow: 2 3");
    }

    private File templateFile(Class<?> class1)
    {
        File template = new File(
            SOURCE_DIR,
            StringUtils.classNameToFilePath(class1.getName())
            + ".jamon");
        return template;
    }

    private void writeTemplate(File template, final String STRING) throws IOException
    {
        FileWriter w = new FileWriter(template);
        w.write(STRING);
        w.close();
        // guarantee that the template is "newer" than the impl after
        // truncation of milliseconds
        template.setLastModified(System.currentTimeMillis() + 1000);
    }
}
