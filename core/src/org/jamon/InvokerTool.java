package org.jamon;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.HashMap;

public class InvokerTool
{
    public static class UsageException
        extends Exception
    {
        String usage()
        {
            return "java "
                + InvokerTool.class.getName()
                + " [-o outputfile] "
                + " [-s templatesourcedir]"
                + " [-w workdir]"
                + " template-path [[arg1=val1] ...]";
        }
    }

    private static void parseArgString(Map p_argMap, String p_arg)
        throws UsageException
    {
        int i = p_arg.indexOf("=");
        if (i <= 0)
        {
            throw new UsageException();
        }
        p_argMap.put(p_arg.substring(0,i),p_arg.substring(i+1));
    }

    private static void invoke(String[] args)
        throws UsageException, IOException
    {
        int a = 0;
        StandardTemplateManager manager = new StandardTemplateManager();
        String outFile = null;
        while (a < args.length && args[a].startsWith("-"))
        {
            if (args[a].startsWith("--workdir="))
            {
                manager.setWorkDir(args[a].substring(10));
            }
            else if (args[a].equals("-w"))
            {
                a++;
                if (a < args.length)
                {
                    manager.setWorkDir(args[a]);
                }
                else
                {
                    throw new UsageException();
                }
            }
            else if (args[a].startsWith("--srcdir="))
            {
                manager.setSourceDir(args[a].substring(9));
            }
            else if (args[a].equals("-s"))
            {
                a++;
                if (a < args.length)
                {
                    manager.setSourceDir(args[a]);
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

        new Invoker(manager, templateName).render(writer,argMap);
    }


    private static void displayError(String p_message)
    {
        System.err.println(p_message);
    }

    public static void main(String[] args)
    {
        try
        {
            invoke(args);
        }
        catch (UsageException e)
        {
            displayError("Usage: " + e.usage());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}
