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
 * Contributor(s):
 */

package org.jamon.integration;

import test.jamon.Recompilation;
import org.jamon.StringUtils;
import java.io.FileWriter;
import java.io.File;

public class RecompilationTest
    extends TestBase
{
    public void testIt()
        throws Exception
    {
        Recompilation template =
            new Recompilation(getTemplateManager())
            .writeTo(getWriter());
        template.render();
        checkOutput("This is the template");

        FileWriter w =
            new FileWriter(new File(SOURCE_DIR,
                                    StringUtils.classNameToFilePath
                                        (template.getClass().getName())));

        final String STRING = "This is changed\n";

        w.write(STRING);
        w.close();
        resetWriter();
        template.writeTo(getWriter()).render();
        checkOutput(STRING);
    }
}
