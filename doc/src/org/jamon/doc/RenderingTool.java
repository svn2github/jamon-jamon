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
 * The Initial Developer of the Original Code is Luis O'Shea.  Portions
 * created by Luis O'Shea are Copyright (C) 2002 Luis O'Shea.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.doc;

import java.io.Writer;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManager;

public class RenderingTool
{

    public static void main(String[] p_arguments)
        throws IOException
    {
        TemplateManager manager = new StandardTemplateManager()
            .setSourceDir(System.getProperty("org.jamon.integration.templatedir"))
            .setWorkDir(System.getProperty("org.jamon.integration.workdir"));
        Writer out = new FileWriter(System.getProperty("org.jamon.integration.out"));
        new RainbowTemplate(manager)
            .writeTo(out)
            .render();
    }

}
