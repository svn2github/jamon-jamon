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

package org.jamon;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.HashMap;

public class InvokerTool
{
    public class UsageException
        extends Exception
    {
        public String toString()
        {
            return "java "
                + InvokerTool.this.getClass().getName()
                + " [-o outputfile] "
                + " [-s templatesourcedir]"
                + " [-w workdir]"
                + " template-path [[arg1=val1] ...]";
        }
    }

    private void parseArgString(Map p_argMap, String p_arg)
        throws UsageException
    {
        int i = p_arg.indexOf("=");
        if (i <= 0)
        {
            throw new UsageException();
        }
        p_argMap.put(p_arg.substring(0,i),p_arg.substring(i+1));
    }

    protected void invoke(String[] args,
                          Invoker.ObjectParser p_objectParser)
        throws UsageException, IOException
    {
        int a = 0;
        StandardTemplateManager.Data data =
            new StandardTemplateManager.Data();
        String outFile = null;
        while (a < args.length && args[a].startsWith("-"))
        {
            if (args[a].startsWith("--workdir="))
            {
                data.setWorkDir(args[a].substring(10));
            }
            else if (args[a].equals("-w"))
            {
                a++;
                if (a < args.length)
                {
                    data.setWorkDir(args[a]);
                }
                else
                {
                    throw new UsageException();
                }
            }
            else if (args[a].startsWith("--srcdir="))
            {
                data.setSourceDir(args[a].substring(9));
            }
            else if (args[a].equals("-s"))
            {
                a++;
                if (a < args.length)
                {
                    data.setSourceDir(args[a]);
                }
                else
                {
                    throw new UsageException();
                }
            }
            else if (args[a].startsWith("--output="))
            {
                outFile = args[a].substring(9);
            }
            else if (args[a].equals("-o"))
            {
                a++;
                if (a < args.length)
                {
                    outFile = args[a];
                }
                else
                {
                    throw new UsageException();
                }
            }
            else
            {
                throw new UsageException();
            }
            a++;
        }
        if (a >= args.length)
        {
            throw new UsageException();
        }

        String templateName = args[a++];

        HashMap argMap = new HashMap();
        while (a < args.length)
        {
            parseArgString(argMap, args[a++]);
        }

        Writer writer = outFile == null
            ? new OutputStreamWriter(System.out)
            : new FileWriter(outFile);

        new Invoker(new StandardTemplateManager(data),
                    templateName,
                    p_objectParser)
            .render(writer, argMap);
    }

    public static void main(String[] args)
    {
        try
        {
            new InvokerTool().invoke(args,null);
        }
        catch (UsageException e)
        {
            System.err.println("Usage: " + e);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}
