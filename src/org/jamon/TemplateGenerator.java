package org.modusponens.jtt;

import java.io.*;
import org.modusponens.jtt.lexer.*;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.parser.*;
import org.modusponens.jtt.analysis.*;

public class TemplateGenerator
{
    public static void main(String [] args)
    {
        try
        {
            File destdir = new File(args[0]);
            destdir.mkdirs();
            if (! destdir.exists() || ! destdir.isDirectory())
            {
                throw new IOException("Unable to create destination dir "
                                      + destdir);
            }

            for (int i = 1; i < args.length; ++i)
            {
                String templateName = args[i];
                int slash = templateName.lastIndexOf('/');
                String pkg = "";
                if (slash == 0)
                {
                    throw new IOException("Can only use relative paths");
                }
                else if (slash > 0)
                {
                    pkg = templateName.substring(0,slash).replace('/','.');
                    templateName = templateName.substring(slash+1);
                }

                System.out.println("Generating interface for "
                                   + pkg + '.' + templateName);
                Parser parser =
                    new Parser(new Lexer(new PushbackReader
                                         (new FileReader(args[i]),
                                          1024)));
                Start tree = parser.parse();

                File javaFile = new File(destdir, pkg.replace('.','/'));
                javaFile.mkdirs();

                FileWriter w =
                    new FileWriter(new File(javaFile, templateName + ".java"));

                InterfaceGenerator g1 =
                    new InterfaceGenerator(w, pkg, templateName);

                tree.apply(g1);
                g1.generateClassSource();
                w.flush();
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
