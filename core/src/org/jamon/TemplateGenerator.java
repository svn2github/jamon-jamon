package org.jamon;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.List;
import org.jamon.lexer.Lexer;
import org.jamon.lexer.LexerException;
import org.jamon.node.Start;
import org.jamon.parser.Parser;
import org.jamon.parser.ParserException;

public class TemplateGenerator
{
    private static final String FILESEP =
        System.getProperty("file.separator");


    private static void generateInterface(File p_destdir,
                                          TemplateDescriber p_describer,
                                          TemplateResolver p_resolver,
                                          String p_pkgPrefix,
                                          String p_filename)
        throws IOException,
               ParserException,
               LexerException
    {
        generateInterface(p_destdir,
                          p_describer,
                          p_resolver,
                          p_pkgPrefix,
                          p_filename,
                          p_filename);
    }


    private static void generateInterface(File p_destdir,
                                          TemplateDescriber p_describer,
                                          TemplateResolver p_resolver,
                                          String p_pkgPrefix,
                                          String p_filename,
                                          String p_absoluteFilename)
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
                + StringUtils.pathToClassName(p_filename.substring(0,fsPos));
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

        File pkgDir = new File(p_destdir, StringUtils.classNameToPath(pkg));
        File javaFile = new File(pkgDir, templateName + ".java");

        System.out.println(p_filename + " => " + javaFile);

        BaseAnalyzer bg =
            new BaseAnalyzer(p_describer.parseTemplate(p_absoluteFilename));

        pkgDir.mkdirs();
        FileWriter writer = new FileWriter(javaFile);

        try
        {
            new IntfGenerator(p_resolver,
                              "/" + p_filename,
                              bg,
                              writer)
                .generateClassSource();
        }
        catch (IOException e)
        {
            writer.close();
            javaFile.delete();
            throw e;
        }
        writer.close();
    }


    public static void generateInterfaces(File p_destDir,
                                          String p_pkgPrefix,
                                          String[] p_relativeFilenames,
                                          String[] p_absoluteFilenames)
        throws IOException,
               ParserException,
               LexerException
    {
        if (p_relativeFilenames.length != p_absoluteFilenames.length)
        {
            throw new RuntimeException("Arrays of relative and absolute filenames have different length");
        }

        p_destDir.mkdirs();
        if (! p_destDir.exists() || ! p_destDir.isDirectory())
        {
            throw new IOException("Unable to create destination dir "
                                  + p_destDir);
        }
        
        TemplateResolver resolver = new TemplateResolver(p_pkgPrefix);
        TemplateDescriber describer = new TemplateDescriber("");
        
        for (int i = 0; i < p_relativeFilenames.length; i++)
        {
            generateInterface(p_destDir, 
                              describer, 
                              resolver, 
                              p_pkgPrefix, 
                              p_relativeFilenames[i],
                              p_absoluteFilenames[i]);
        }
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

            TemplateDescriber describer = new TemplateDescriber("");

            while (arg < args.length)
            {
                generateInterface(destdir, describer, resolver, pkgPrefix, args[arg++]);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
