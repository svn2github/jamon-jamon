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
import java.io.FileOutputStream;
import java.io.IOException;

import org.jamon.util.StringUtils;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.Analyzer;
import org.jamon.codegen.ImplGenerator;
import org.jamon.codegen.ProxyGenerator;
import org.jamon.codegen.TemplateUnit;
import org.jamon.emit.EmitMode;

public class TemplateProcessor
{
    public TemplateProcessor(File p_destDir,
                             File p_sourceDir,
                             ClassLoader p_classLoader,
                             EmitMode p_emitMode)
    {
        m_destDir = p_destDir;
        m_emitMode = p_emitMode;
        m_describer =
            new TemplateDescriber(new FileTemplateSource(p_sourceDir),
                                  p_classLoader);
    }

    private final File m_destDir;
    private final TemplateDescriber m_describer;
    private final EmitMode m_emitMode;

    public void generateSource(String p_filename)
        throws IOException
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
        FileOutputStream out = new FileOutputStream(javaFile);

        try
        {
            new ProxyGenerator(out, m_describer, templateUnit)
                .generateClassSource();
        }
        catch (RuntimeException e)
        {
            try
            {
                out.close();
                javaFile.delete();
            }
            finally
            {
            }
            throw e;
        }
        catch (IOException e)
        {
            try
            {
                out.close();
                javaFile.delete();
            }
            finally
            {
            }
            throw e;
        }
        out.close();

        javaFile = new File(pkgDir, className + "Impl.java");
        out = new FileOutputStream(javaFile);
        try
        {
            new ImplGenerator(out, m_describer, templateUnit, m_emitMode)
                .generateSource();
        }
        catch (RuntimeException e)
        {
            try
            {
                out.close();
                javaFile.delete();
            }
            finally
            {
            }
            throw e;
        }
        catch (IOException e)
        {
            try
            {
                out.close();
                javaFile.delete();
            }
            finally
            {
            }
            throw e;
        }
        out.close();
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
    private static final String EMITMODE = "--emitMode=";

    public static void main(String [] args)
    {
        try
        {
            int arg = 0;
            File sourceDir = new File(".");
            File destDir = null;
            EmitMode emitMode = EmitMode.STANDARD;
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
                else if (args[arg].startsWith(EMITMODE))
                {
                    String modeName = args[arg].substring(EMITMODE.length());
                    emitMode = EmitMode.fromString(modeName);
                    if (emitMode == null)
                    {
                        System.err.println("Unknown emit mode: " + modeName);
                        showHelp();
                        System.exit(1);
                    }
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
                                      TemplateProcessor.class.getClassLoader(),
                                      emitMode);

            while (arg < args.length)
            {
                processor.generateSource(args[arg++]);
            }
        }
        catch (JamonTemplateException e)
        {
            System.err.println(e.getFileName() + ":" + e.getLine() + ":"
                               + e.getColumn() + ":" + e.getMessage());
            System.exit(2);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
