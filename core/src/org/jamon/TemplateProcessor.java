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
    public TemplateProcessor(File p_destdir,
                             TemplateDescriber p_describer,
                             TemplateResolver p_resolver,
                             boolean p_generateImpls)
    {
        m_destdir = p_destdir;
        m_describer=p_describer;
        m_resolver=p_resolver;
        m_generateImpls=p_generateImpls;
    }

    private File m_destdir;
    private TemplateDescriber m_describer;
    private TemplateResolver m_resolver;
    private boolean m_generateImpls;

    public void generateSource(String p_filename)
        throws IOException,
               ParserException,
               LexerException
    {
        System.out.println(p_filename);
        String templateName = p_filename;
        String pkg = "";
        int fsPos = templateName.lastIndexOf(File.separator);
        if (fsPos == 0)
        {
            throw new IOException("Can only use relative paths");
        }
        else if (fsPos > 0)
        {
            pkg = StringUtils.pathToClassName(p_filename.substring(0,fsPos));
            templateName =
                templateName.substring(fsPos+File.separator.length());
        }

        File pkgDir = new File(m_destdir, StringUtils.classNameToPath(pkg));
        File javaFile = new File(pkgDir, templateName + ".java");

        BaseAnalyzer analyzer =
            m_generateImpls
            ? new ImplAnalyzer(p_filename,
                               m_describer.parseTemplate(p_filename))
            : new BaseAnalyzer(m_describer.parseTemplate(p_filename));

        pkgDir.mkdirs();
        FileWriter writer = new FileWriter(javaFile);

        try
        {
            new IntfGenerator(m_resolver,
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

        if (m_generateImpls)
        {
            javaFile = new File(pkgDir, templateName + "Impl.java");
            writer = new FileWriter(javaFile);
            try
            {
                new ImplGenerator(writer,
                                  m_resolver,
                                  m_describer,
                                  (ImplAnalyzer)analyzer)
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

            TemplateProcessor processor =
                new TemplateProcessor(destDir,
                                      new TemplateDescriber(sourceDir),
                                      new TemplateResolver(),
                                      doBoth);

            while (arg < args.length)
            {
                processor.generateSource(args[arg++]);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
