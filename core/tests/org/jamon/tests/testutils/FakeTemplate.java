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

package org.jamon.tests.testutils;

import org.jamon.AbstractTemplateProxy;

public class FakeTemplate
    extends AbstractTemplateProxy
{

    public FakeTemplate(org.jamon.TemplateManager p_manager)
    {
        super(p_manager);
    }

    protected interface Intf
        extends org.jamon.AbstractTemplateProxy.Intf{

        void render(final boolean p__jamon__b, final String p__jamon__s)
            throws java.io.IOException;

        void setI(int i);
    }


    protected String getPath()
    {
        return "/org/jamon/tests/testutils/FakeTemplate";
    }

    private Intf getInstance()
        throws java.io.IOException
    {
        return (Intf) getUntypedInstance();
    }

    public final FakeTemplate setI(int p_i)
        throws java.io.IOException
    {
        getInstance().setI(p_i);
        return this;
    }

    public void render(final boolean p__jamon__b, final String p__jamon__s)
        throws java.io.IOException
    {
        try
        {
            getInstance().render(p__jamon__b, p__jamon__s);
        }
        finally
        {
            releaseInstance();
        }
    }


    public FakeTemplate writeTo(java.io.Writer p_writer)
        throws java.io.IOException
    {
        getInstance().writeTo(p_writer);
        return this;
    }

    public FakeTemplate escaping(org.jamon.escaping.Escaping p_escaping)
    {
        escape(p_escaping);
        return this;
    }

    public FakeTemplate autoFlush(boolean p_autoFlush)
        throws java.io.IOException
    {
        getInstance().autoFlush(p_autoFlush);
        return this;
    }

}
