package org.modusponens.jtt;

import java.io.*;
import org.modusponens.jtt.lexer.*;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.parser.*;
import org.modusponens.jtt.analysis.*;

public class ParseTest
{
    public static void main(String [] args)
    {
        try
        {
            Parser parser =
                new Parser(new Lexer(new PushbackReader
                                     (new FileReader(args[0]),
                                      1024)));
            // Parse the input
            Start tree = parser.parse();

            // System.out.println(tree);
            tree.apply(new PrettyPrinterAdapter());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
