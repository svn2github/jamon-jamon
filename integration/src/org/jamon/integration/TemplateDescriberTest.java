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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2002 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s): Jay Sachs
 */

package org.jamon.integration;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.jamon.codegen.TemplateDescriber;
import org.jamon.FileTemplateSource;

/**
 * Test Jamon's java escapes.  See "Jamon User's Guide", section 2.
 **/

public class TemplateDescriberTest
    extends TestCase
{
    public void testArgumentIntrospection()
        throws Exception
    {
        File nonexistent = File.createTempFile("jamontest",null);
        nonexistent.deleteOnExit();
        TemplateDescriber describer = new TemplateDescriber
            (new FileTemplateSource(nonexistent));
        List argNames = describer.getRequiredArgNames("/test/jamon/ClassOnly");
        assertEquals(2, argNames.size());
        assertEquals("i", argNames.get(0));
        assertEquals("j", argNames.get(1));
    }

}
