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
            Parser parser =
                new Parser(new Lexer(new PushbackReader
                                     (new FileReader(args[0]),
                                      1024)));
            Start tree = parser.parse();

            OutputStreamWriter w = new OutputStreamWriter(System.out);
            InterfaceGenerator g1 =
                new InterfaceGenerator(w,
                                       "test.jtt",
                                       "TestTemplate");
            tree.apply(g1);
            g1.generateClassSource();
            w.flush();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
