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
 * The Original Code is Jamon code, released ??.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Invoker
{
    Invoker(String[] args)
        throws UsageException,
               InvalidTemplateException
    {
        if (args.length == 0)
        {
            throw new UsageException();
        }
        try
        {
            StandardTemplateManager manager = new StandardTemplateManager();
            Class templateClass = Class.forName(args[0]);
            Constructor con = templateClass.getConstructor
                ( new Class[] { TemplateManager.class } );
            m_template = (AbstractTemplateProxy)
                con.newInstance(new Object[] { manager } );
            Method setWriterMethod =
                templateClass.getMethod("setWriter",
                                        new Class [] { Writer.class } );
            setWriterMethod.invoke
                (m_template,
                 new Object[] { new OutputStreamWriter(System.out) });

            m_renderMethod =
                templateClass.getMethod("render", new Class [] { } );
        }
        catch (Error e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new InvalidTemplateException(args[0]);
        }
    }

    private final AbstractTemplateProxy m_template;
    private final Method m_renderMethod;

    void renderTemplate()
        throws IOException, ClassNotFoundException
    {
        try
        {
            m_renderMethod.invoke(m_template, new Object[] { });
        }
        catch (Error e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            displayError("Unable to render template");
        }
    }

    private static void displayError(String p_message)
    {
        System.err.println(p_message);
    }

    public static void main(String[] args)
    {
        try
        {
            new Invoker(args).renderTemplate();
        }
        catch (UsageException e)
        {
            displayError(e.usage());
        }
        catch (InvalidTemplateException e)
        {
            displayError(e.getMessage());
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }

    private static class InvalidTemplateException
        extends Exception
    {
        InvalidTemplateException(String p_templateName)
        {
            super(p_templateName
                  + " does not appear to be a valid template class");
        }
    }


    private static class UsageException
        extends Exception
    {
        String usage()
        {
            return "java "
                + Invoker.class.getName()
                + " template-name [[arg1=val1] ...]";
        }
    }

}
