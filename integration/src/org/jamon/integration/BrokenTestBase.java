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
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.integration;

import java.io.File;

import org.jamon.TemplateProcessor;
import org.jamon.JamonException;

public class BrokenTestBase
    extends AbstractTestBase
{
    private TemplateProcessor m_processor = null;

    public void setUp()
        throws Exception
    {
        String integrationDir =
            System.getProperty("org.jamon.integration.basedir");
        m_processor =
            new TemplateProcessor(new File(integrationDir + "/build/src"),
                                  new File(integrationDir + "/templates"),
                                  getClass().getClassLoader());
    }

    public void generateSource(String p_path)
        throws Exception
    {
        m_processor.generateSource(p_path);
    }
}
