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

package org.jamon.doc;

import java.io.IOException;
import java.io.FileWriter;
import java.io.Writer;
import org.jamon.Renderer;

import org.jamon.doc.tutorial.*;

public class DocBuilder
{
    private static String s_docDir;

    public static void main(String args[])
    {
        s_docDir = args[0];
        try
        {
            render(new Overview().makeRenderer(), "Overview");
            render(new BuildingProjects().makeRenderer(), "BuildingProjects");
            render(new UserGuide().makeRenderer(), "UserGuide");
            render(new GeneratedTemplates().makeRenderer(),
                   "GeneratedTemplates");
            render(new Reference().makeRenderer(), "Reference");
            render(new index().makeRenderer(), "index");
            render(new JamonTask().makeRenderer(), "JamonTask");
            render(new InvokerTask().makeRenderer(), "InvokerTask");
            render(new Contact().makeRenderer(), "Contact");
            render(new About().makeRenderer(), "About");
            render(new News().makeRenderer(), "News");
            render(new Features().makeRenderer(), "Features");
            render(new Download().makeRenderer(), "Download");
            render(new Servlets().makeRenderer(), "Servlets");
            render(new TutorialPath().makeRenderer(), "tutorial/TutorialPath");
            render(new TutorialSample1().makeRenderer(),
                   "tutorial/TutorialSample1");
            render(new TutorialSample2().makeRenderer(),
                   "tutorial/TutorialSample2");
            render(new TutorialSample3().makeRenderer(),
                   "tutorial/TutorialSample3");
            render(new TutorialSample4().makeRenderer(),
                   "tutorial/TutorialSample4");
            render(new TutorialSample5().makeRenderer(),
                   "tutorial/TutorialSample5");
            render(new TutorialSample6().makeRenderer(),
                   "tutorial/TutorialSample6");
            render(new TutorialSample7().makeRenderer(),
                   "tutorial/TutorialSample7");
            render(new TutorialSample8().makeRenderer(),
                   "tutorial/TutorialSample8");
            render(new TutorialSample9().makeRenderer(),
                   "tutorial/TutorialSample9");
            render(new TutorialSample10().makeRenderer(),
                   "tutorial/TutorialSample10");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void render(Renderer p_renderer, String p_name)
        throws IOException
    {
        Writer writer = new FileWriter(s_docDir + "/" + p_name + ".html");
        p_renderer.renderTo(writer);
        writer.close();
    }

}
