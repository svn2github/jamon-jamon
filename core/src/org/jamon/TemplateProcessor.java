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
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jamon.util.StringUtils;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateResolver;
import org.jamon.codegen.Analyzer;
import org.jamon.codegen.ImplGenerator;
import org.jamon.codegen.ProxyGenerator;
import org.jamon.codegen.TemplateUnit;
import org.jamon.lexer.Lexer;
import org.jamon.lexer.LexerException;
import org.jamon.node.Start;
import org.jamon.parser.Parser;
import org.jamon.parser.ParserException;

public class TemplateProcessor
{
    public TemplateProcessor(File p_destDir,
                             File p_sourceDir,
                             ClassLoader p_classLoader)
    {
        m_destDir = p_destDir;
        m_describer =
            new TemplateDescriber(new FileTemplateSource(p_sourceDir),
                                  p_classLoader);
        m_resolver = new TemplateResolver();
    }

    private File m_destDir;
    private TemplateDescriber m_describer;
    private TemplateResolver m_resolver;

    public void generateSource(String p_filename)
        throws IOException,
               ParserException,
               LexerException
    {
        int pPos = p_filename.indexOf('.');
        String templateName =
            pPos < 0 ? p_filename : p_filename.substring(0,pPos);
        String pkg = "";
        int fsPos = templateName.lastIndexOf(File.separator);
        String className = templateName;
        if (fsPos == 0)
        {
            throw new IOException("Can only use relative paths");
        }
        else if (fsPos > 0)
        {
            pkg = StringUtils.filePathToClassName
                (templateName.substring(0,fsPos));
            className =
                templateName.substring(fsPos+File.separator.length());
        }

        File pkgDir = new File(m_destDir,
                               StringUtils.classNameToFilePath(pkg));
        File javaFile = new File(pkgDir, className + ".java");

        TemplateUnit templateUnit = new Analyzer
            ("/" + StringUtils.filePathToTemplatePath(templateName),
             m_describer)
            .analyze();
        pkgDir.mkdirs();
        FileWriter writer = new FileWriter(javaFile);

        try
        {
            new ProxyGenerator(writer, m_resolver, m_describer, templateUnit)
                .generateClassSource();
        }
        catch (RuntimeException e)
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

        javaFile = new File(pkgDir, className + "Impl.java");
        writer = new FileWriter(javaFile);
        try
        {
            new ImplGenerator(writer,
                              m_resolver,
                              m_describer,
                              templateUnit)
                .generateSource();
        }
        catch (RuntimeException e)
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

    private static void showHelp()
    {
        System.out.println("Usage: java org.jamon.TemplateProcessor <args> templatePath*");
        System.out.println("  Arguments:");
        System.out.println("  -h|--help         - print this help");
        System.out.println("  "
                           + DESTDIR
                           + "<path>  - path to where compiled .java files go (required)");
        System.out.println("  "
                           + SRCDIR
                           + "<path>   - path to template directory");
    }

    private static final String DESTDIR = "--destDir=";
    private static final String SRCDIR = "--srcDir=";

    public static void main(String [] args)
    {
        try
        {
            int arg = 0;
            File sourceDir = new File(".");
            File destDir = null;
            while (arg<args.length && args[arg].startsWith("-"))
            {
                if ("-h".equals(args[arg]) || "--help".equals(args[arg]))
                {
                    showHelp();
                    System.exit(0);
                }
                else if (args[arg].startsWith(DESTDIR))
                {
                    destDir = new File(args[arg].substring(DESTDIR.length()));
                }
                else if (args[arg].startsWith(SRCDIR))
                {
                    sourceDir = new File(args[arg].substring(SRCDIR.length()));
                }
                else
                {
                    System.err.println("Unknown option: " + args[arg]);
                    showHelp();
                    System.exit(1);
                }
                arg++;
            }
            if (destDir==null)
            {
                System.err.println("You must specify " + DESTDIR);
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
                                      sourceDir,
                                      TemplateProcessor.class.getClassLoader());

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
