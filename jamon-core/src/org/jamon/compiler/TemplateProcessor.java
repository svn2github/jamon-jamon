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

package org.jamon.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.jamon.api.ParsedTemplate;
import org.jamon.api.SourceGenerator;
import org.jamon.api.TemplateParser;
import org.jamon.codegen.TemplateParserImpl;
import org.jamon.util.StringUtils;

public class TemplateProcessor
{
    public TemplateProcessor(File p_destDir,
                             File p_sourceDir,
                             ClassLoader p_classLoader)
    {
        m_destDir = p_destDir;
        m_parser = new TemplateParserImpl(new FileTemplateSource(p_sourceDir), p_classLoader);
    }

    private final File m_destDir;
    private final TemplateParser m_parser;

    public void generateSource(String p_filename)
        throws IOException
    {
        // strip suffix, if any
        int pPos = p_filename.indexOf('.');
        String templateName =
            pPos < 0 ? p_filename : p_filename.substring(0,pPos);

        File pkgDir = new File(m_destDir, templateName).getParentFile();

        ParsedTemplate parsedTemplate =
            m_parser.parseTemplate("/" + StringUtils.filePathToTemplatePath(templateName));
        pkgDir.mkdirs();
        generateSource(
            new File(m_destDir, templateName + ".java"), parsedTemplate.getProxyGenerator());
        generateSource(
            new File(m_destDir, templateName + "Impl.java"), parsedTemplate.getImplGenerator());
    }

    private void generateSource(File javaFile, SourceGenerator sourceGenerator)
        throws IOException
    {
        FileOutputStream out = new FileOutputStream(javaFile);
        boolean success = false;
        try
        {
            sourceGenerator.generateSource(out);
            success = true;
        }
        finally
        {
            out.close();
            if (!success)
            {
                javaFile.delete();
            }
        }
    }

    private static void showHelp()
    {
        System.out.println("Usage: java org.jamon.TemplateProcessor <args> templatePath*");
        System.out.println("  Arguments:");
        System.out.println("  -h|--help         - print this help");
        System.out.println(
            "  -d|--directories  - treat paths as directories, " +
            "                      and parse all .jamon files therein");
        System.out.println(
            "  "
            + DESTDIR
            + "<path>  - path to where compiled .java files go (required)");
        System.out.println("  "
                           + SRCDIR
                           + "<path>   - path to template directory");
        //FIXME - autogenerate list of allowable emit modes
        System.out.println(
            "  "
            + EMITMODE
            + "<emitMode>  - emit mode to use - one of Standard, Limited or Strict");
        System.out.println(
            "  "
            + CONTEXTTYPE
            + "<contextType>  - class type for jamonContext variable; defaults to java.lang.Object");

    }

    private static final String DESTDIR = "--destDir=";
    private static final String SRCDIR = "--srcDir=";
    private static final String EMITMODE = "--emitMode=";
    private static final String CONTEXTTYPE = "--contextType";

    public static void main(String [] args)
    {
        try
        {
            int arg = 0;
            boolean processDirectories = false;
            File sourceDir = new File(".");
            File destDir = null;
            while (arg < args.length && args[arg].startsWith("-"))
            {
                if ("-h".equals(args[arg]) || "--help".equals(args[arg]))
                {
                    showHelp();
                    System.exit(0);
                }
                else if ("-d".equals(args[arg])
                         || "--directories".equals(args[arg]))
                {
                    processDirectories = true;
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
                return;  //silence warning about possibly null destDir
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
                if (processDirectories)
                {
                    String directoryName = args[arg++];
                    String fullPath = sourceDir + directoryName;
                    File directory = new File(fullPath);
                    if (!directory.isDirectory())
                    {
                        System.err.println(fullPath + " is not a directory");
                    }
                    File[] files = directory.listFiles(new FilenameFilter() {
                        public boolean accept(File p_dir, String p_name)
                        {
                            return p_name.endsWith(".jamon");
                        }
                    });
                    for (int i = 0; i < files.length; i++)
                    {
                        processor.generateSource(
                            directoryName + "/" + files[i].getName());
                    }
                }
                else
                {
                    processor.generateSource(args[arg++]);
                }
            }
        }
        catch (ParserErrorsImpl e)
        {
            e.printErrors(System.err);
            System.exit(2);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
