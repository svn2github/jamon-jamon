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

package org.jamon.tests.templates.escape;

import java.io.IOException;
import java.io.Writer;

import org.jamon.Renderer;
import org.jamon.AbstractTemplateImpl;
import org.jamon.escaping.Escaping;
import org.jamon.escape._escape;

public class TestBase
    extends org.jamon.tests.templates.TestBase
{
    protected class Fragment
        extends AbstractTemplateImpl
        implements _escape.Fragment_content
    {
        Fragment(String p_body)
        {
            super(TestBase.this.getTemplateManager(), Escaping.NONE);
            m_body = p_body;
        }
        private final String m_body;
        public void render()
            throws IOException
        {
            writeEscaped(m_body, Escaping.NONE);
        }

        public Renderer makeRenderer()
        {
            return new Renderer()
                {
                    public void renderTo(Writer p_writer)
                        throws IOException
                    {
                        writeTo(p_writer);
                        render();
                    }
                };
        }
    }
}
