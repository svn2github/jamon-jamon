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
import org.jamon.util.StringUtils;
import java.io.FileWriter;
import java.io.File;

public class RecompilationTest
    extends TestBase
{
    public void testIt()
        throws Exception
    {
        new Recompilation(getRecompilingTemplateManager()).render(getWriter());
        checkOutput("This is the template");

        FileWriter w =
            new FileWriter(new File(SOURCE_DIR,
                                    StringUtils.classNameToFilePath
                                        (Recompilation.class.getName())
                                    + ".jamon"));

        final String STRING = "This is changed\n";

        w.write(STRING);
        w.close();
        resetWriter();
        new Recompilation(getRecompilingTemplateManager()).render(getWriter());
        checkOutput(STRING);
    }
}
