package org.modusponens.jtt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.List;
import org.modusponens.jtt.lexer.Lexer;
import org.modusponens.jtt.lexer.LexerException;
import org.modusponens.jtt.node.Start;
import org.modusponens.jtt.parser.Parser;
import org.modusponens.jtt.parser.ParserException;

public class TemplateGenerator
{
    private static final String FILESEP =
        System.getProperty("file.separator");

    private static void generateInterface(File p_destdir,
                                          TemplateResolver p_resolver,
                                          String p_pkgPrefix,
                                          String p_filename)
        throws IOException,
               ParserException,
               LexerException
    {
        String templateName = p_filename;
        String pkg = p_pkgPrefix;
        int fsPos = templateName.lastIndexOf(FILESEP);
        if (fsPos == 0)
        {
            throw new IOException("Can only use relative paths");
        }
        else if (fsPos > 0)
        {
            pkg = p_pkgPrefix
                + PathUtils.pathToClassName(p_filename.substring(0,fsPos));
            templateName = templateName.substring(fsPos+FILESEP.length());
        }
        else
        {
            int dot = pkg.lastIndexOf('.');
            if (dot == pkg.length() - 1)
            {
                pkg = pkg.substring(0,dot);
            }
        }

        File pkgDir = new File(p_destdir, PathUtils.classNameToPath(pkg));
        File javaFile = new File(pkgDir, templateName + ".java");

        System.out.println(p_filename + " => " + javaFile);

        Parser parser =
            new Parser(new Lexer(new PushbackReader
                                 (new FileReader(p_filename),
                                  1024)));
        Start tree = parser.parse();

        pkgDir.mkdirs();
        FileWriter writer = new FileWriter(javaFile);

        InterfaceGenerator intfGen =
            new InterfaceGenerator(p_resolver, "/" + p_filename);

        tree.apply(intfGen);
        try
        {
            intfGen.generateClassSource(writer);
        }
        catch (IOException e)
        {
            writer.close();
            javaFile.delete();
            throw e;
        }
        writer.close();
    }

    public static void main(String [] args)
    {
        try
        {
            int arg = 0;
            File destdir = new File(args[arg++]);
            destdir.mkdirs();
            if (! destdir.exists() || ! destdir.isDirectory())
            {
                throw new IOException("Unable to create destination dir "
                                      + destdir);
            }

            String pkgPrefix = args[arg++];
            TemplateResolver resolver = new TemplateResolver(pkgPrefix);

            while (arg < args.length)
            {
                generateInterface(destdir, resolver, pkgPrefix, args[arg++]);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
