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

public class TemplateProcessor
{
    private static final String FILESEP =
        System.getProperty("file.separator");

    private static void generateImplAndInterface(File p_destdir,
                                                 TemplateDescriber p_describer,
                                                 TemplateResolver p_resolver,
                                                 String p_filename)
        throws IOException,
               ParserException,
               LexerException
    {
        System.out.println(p_filename);
        String templateName = p_filename;
        String pkg = "";
        int fsPos = templateName.lastIndexOf(FILESEP);
        if (fsPos == 0)
        {
            throw new IOException("Can only use relative paths");
        }
        else if (fsPos > 0)
        {
            pkg = StringUtils.pathToClassName(p_filename.substring(0,fsPos));
            templateName = templateName.substring(fsPos+FILESEP.length());
        }

        File pkgDir = new File(p_destdir, StringUtils.classNameToPath(pkg));
        File javaFile = new File(pkgDir, templateName + ".java");

        ImplAnalyzer analyzer =
            new ImplAnalyzer(p_filename,
                             p_describer.parseTemplate(p_filename));

        pkgDir.mkdirs();
        FileWriter writer = new FileWriter(javaFile);

        try
        {
            new IntfGenerator(p_resolver,
                              "/" + p_filename,
                              analyzer,
                              writer)
                .generateClassSource();
        }
        catch (IOException e)
        {
            try
            {
                writer.close();
                javaFile.delete();
            }
            catch (IOException e2)
            {
                throw e;
            }
        }
        writer.close();

        javaFile = new File(pkgDir, templateName + "Impl.java");
        writer = new FileWriter(javaFile);
        try
        {
            new ImplGenerator(writer,
                              p_resolver,
                              p_describer,
                              analyzer)
                .generateSource();
        }
        catch (IOException e)
        {
            try
            {
                writer.close();
                javaFile.delete();
            }
            finally
            {
                e.printStackTrace();
                throw e;
            }
        }
        writer.close();

    }


    private static void generateInterface(File p_destdir,
                                          TemplateDescriber p_describer,
                                          TemplateResolver p_resolver,
                                          String p_filename)
        throws IOException,
               ParserException,
               LexerException
    {
        System.out.println(p_filename);
        String templateName = p_filename;
        String pkg = "";
        int fsPos = templateName.lastIndexOf(FILESEP);
        if (fsPos == 0)
        {
            throw new IOException("Can only use relative paths");
        }
        else if (fsPos > 0)
        {
            pkg = StringUtils.pathToClassName(p_filename.substring(0,fsPos));
            templateName = templateName.substring(fsPos+FILESEP.length());
        }

        File pkgDir = new File(p_destdir, StringUtils.classNameToPath(pkg));
        File javaFile = new File(pkgDir, templateName + ".java");

        BaseAnalyzer analyzer =
            new BaseAnalyzer(p_describer.parseTemplate(p_filename));

        pkgDir.mkdirs();
        FileWriter writer = new FileWriter(javaFile);

        try
        {
            new IntfGenerator(p_resolver,
                              "/" + p_filename,
                              analyzer,
                              writer)
                .generateClassSource();
        }
        catch (IOException e)
        {
            try
            {
                writer.close();
                javaFile.delete();
            }
            finally
            {
                throw e;
            }
        }
        writer.close();
    }


    public static void generateImplAndInterfaces(File p_destDir,
                                                 String p_srcDir,
                                                 String[] p_relativeFilenames)
        throws IOException,
               ParserException,
               LexerException
    {
        p_destDir.mkdirs();
        if (! p_destDir.exists() || ! p_destDir.isDirectory())
        {
            throw new IOException("Unable to create destination dir "
                                  + p_destDir);
        }

        TemplateResolver resolver = new TemplateResolver();
        TemplateDescriber describer =
            new TemplateDescriber(new File(p_srcDir));

        for (int i = 0; i < p_relativeFilenames.length; i++)
        {
            generateImplAndInterface(p_destDir,
                                     describer,
                                     resolver,
                                     p_relativeFilenames[i]);
        }
    }

    public static void generateInterfaces(File p_destDir,
                                          String p_srcDir,
                                          String[] p_relativeFilenames)
        throws IOException,
               ParserException,
               LexerException
    {
        p_destDir.mkdirs();
        if (! p_destDir.exists() || ! p_destDir.isDirectory())
        {
            throw new IOException("Unable to create destination dir "
                                  + p_destDir);
        }

        TemplateResolver resolver = new TemplateResolver();
        TemplateDescriber describer =
            new TemplateDescriber(new File(p_srcDir));

        for (int i = 0; i < p_relativeFilenames.length; i++)
        {
            generateInterface(p_destDir,
                              describer,
                              resolver,
                              p_relativeFilenames[i]);
        }
    }


    private static void showHelp()
    {
        System.out.println("Usage: java org.jamon.TemplateProcessor <args> templatePath*");
        System.out.println("  Arguments:");
        System.out.println("  -h|--help         - print this help");
        System.out.println("  -a|--all         - generate impls too");
        System.out.println("  -a|--all         - generate impls too");
        System.out.println("  -destDir=<path>  - path to where compiled .java files go (required)");
        System.out.println("  -sourceDir=<path>  - path to template directory");
    }

    public static void main(String [] args)
    {
        try
        {
            int arg = 0;
            boolean doBoth = false;
            File sourceDir = new File(".");
            File destDir = null;
            while (arg<args.length && args[arg].startsWith("-"))
            {
                if ("-h".equals(args[arg]) || "--help".equals(args[arg]))
                {
                    showHelp();
                    System.exit(0);
                }
                else if ("-a".equals(args[arg]) || "--all".equals(args[arg]))
                {
                    doBoth = true;
                }
                else if (args[arg].startsWith("--destDir="))
                {
                    destDir = new File(args[arg].substring(10));
                }
                else if (args[arg].startsWith("--sourceDir="))
                {
                    sourceDir = new File(args[arg].substring(12));
                }
                arg++;
            }
            if (destDir==null)
            {
                System.err.println("You must specify --destDir");
                showHelp();
                System.exit(1);
            }

            destDir.mkdirs();
            if (! destDir.exists() || ! destDir.isDirectory())
            {
                throw new IOException("Unable to create destination dir "
                                      + destDir);
            }

            TemplateResolver resolver = new TemplateResolver();
            TemplateDescriber describer = new TemplateDescriber(sourceDir);

            while (arg < args.length)
            {
                String template = args[arg++];
                if (doBoth)
                {
                    generateImplAndInterface(destDir,
                                             describer,
                                             resolver,
                                             template);
                }
                else
                {
                    generateInterface(destDir,
                                      describer,
                                      resolver,
                                      template);
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
