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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jamon.util.StringUtils;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateResolver;
import org.jamon.codegen.ImplAnalyzer;
import org.jamon.codegen.BaseAnalyzer;
import org.jamon.codegen.ImplGenerator;
import org.jamon.codegen.IntfGenerator;
import org.jamon.lexer.Lexer;
import org.jamon.lexer.LexerException;
import org.jamon.node.Start;
import org.jamon.parser.Parser;
import org.jamon.parser.ParserException;

public class TemplateProcessor
{
    public TemplateProcessor(File p_destDir,
                             File p_sourceDir,
                             boolean p_generateImpls)
    {
        m_destDir = p_destDir;
        m_describer =
            new TemplateDescriber(new FileTemplateSource(p_sourceDir));
        m_resolver = new TemplateResolver();
        m_generateImpls = p_generateImpls;
    }

    private File m_destDir;
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
            pkg = StringUtils.filePathToClassName
                (p_filename.substring(0,fsPos));
            templateName =
                templateName.substring(fsPos+File.separator.length());
        }

        File pkgDir = new File(m_destDir,
                               StringUtils.classNameToFilePath(pkg));
        File javaFile = new File(pkgDir, templateName + ".java");

        BaseAnalyzer analyzer =
            m_generateImpls
            ? new ImplAnalyzer(StringUtils.filePathToTemplatePath(p_filename),
                               m_describer.parseTemplate(p_filename))
            : new BaseAnalyzer(m_describer.parseTemplate(p_filename));

        pkgDir.mkdirs();
        FileWriter writer = new FileWriter(javaFile);

        try
        {
            new IntfGenerator(m_resolver,
                              StringUtils.filePathToTemplatePath("/" + p_filename),
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
        System.out.println("  --destDir=<path>  - path to where compiled .java files go (required)");
        System.out.println("  --sourceDir=<path>  - path to template directory");
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
                new TemplateProcessor(destDir, sourceDir, doBoth);

            while (arg < args.length)
            {
                processor.generateSource(args[arg++]);
            }
        }
        catch (JamonParseException e)
        {
            System.err.println(e.getStandardMessage());
            System.exit(2);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
