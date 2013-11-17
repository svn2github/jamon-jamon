/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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

public class TemplateProcessor {
  public TemplateProcessor(File destDir, File sourceDir, ClassLoader classLoader) {
    this.destDir = destDir;
    parser = new TemplateParserImpl(new FileTemplateSource(sourceDir), classLoader);
  }

  private final File destDir;

  private final TemplateParser parser;

  public void generateSource(String filename) throws IOException {
    // strip suffix, if any
    int pPos = filename.indexOf('.');
    String templateName = pPos < 0
        ? filename
        : filename.substring(0, pPos);

    File pkgDir = new File(destDir, templateName).getParentFile();

    ParsedTemplate parsedTemplate = parser.parseTemplate("/"
      + StringUtils.filePathToTemplatePath(templateName));
    pkgDir.mkdirs();
    generateSource(new File(destDir, templateName + ".java"), parsedTemplate.getProxyGenerator());
    generateSource(
      new File(destDir, templateName + "Impl.java"), parsedTemplate .getImplGenerator());
  }

  private void generateSource(File javaFile, SourceGenerator sourceGenerator) throws IOException {
    FileOutputStream out = new FileOutputStream(javaFile);
    boolean success = false;
    try {
      sourceGenerator.generateSource(out);
      success = true;
    }
    finally {
      out.close();
      if (!success) {
        javaFile.delete();
      }
    }
  }

  private static void showHelp() {
    System.out.println("Usage: java org.jamon.TemplateProcessor <args> templatePath*");
    System.out.println("  Arguments:");
    System.out.println("  -h|--help         - print this help");
    System.out.println("  -d|--directories  - treat paths as directories, "
      + "                      and parse all .jamon files therein");
    System.out.println("  " + DESTDIR
      + "<path>  - path to where compiled .java files go (required)");
    System.out.println("  " + SRCDIR + "<path>   - path to template directory");
    // FIXME - autogenerate list of allowable emit modes
    System.out.println("  " + EMITMODE
      + "<emitMode>  - emit mode to use - one of Standard, Limited or Strict");
    System.out.println("  " + CONTEXTTYPE
      + "<contextType>  - class type for jamonContext variable; defaults to java.lang.Object");

  }

  private static final String DESTDIR = "--destDir=";

  private static final String SRCDIR = "--srcDir=";

  private static final String EMITMODE = "--emitMode=";

  private static final String CONTEXTTYPE = "--contextType";

  public static void main(String[] args) {
    try {
      int arg = 0;
      boolean processDirectories = false;
      File sourceDir = new File(".");
      File destDir = null;
      while (arg < args.length && args[arg].startsWith("-")) {
        if ("-h".equals(args[arg]) || "--help".equals(args[arg])) {
          showHelp();
          System.exit(0);
        }
        else if ("-d".equals(args[arg]) || "--directories".equals(args[arg])) {
          processDirectories = true;
        }
        else if (args[arg].startsWith(DESTDIR)) {
          destDir = new File(args[arg].substring(DESTDIR.length()));
        }
        else if (args[arg].startsWith(SRCDIR)) {
          sourceDir = new File(args[arg].substring(SRCDIR.length()));
        }
        else {
          System.err.println("Unknown option: " + args[arg]);
          showHelp();
          System.exit(1);
        }
        arg++;
      }
      if (destDir == null) {
        System.err.println("You must specify " + DESTDIR);
        showHelp();
        System.exit(1);
        return; // silence warning about possibly null destDir
      }

      destDir.mkdirs();
      if (!destDir.exists() || !destDir.isDirectory()) {
        throw new IOException("Unable to create destination dir " + destDir);
      }

      TemplateProcessor processor = new TemplateProcessor(destDir, sourceDir,
          TemplateProcessor.class.getClassLoader());

      while (arg < args.length) {
        if (processDirectories) {
          String directoryName = args[arg++];
          String fullPath = sourceDir + directoryName;
          File directory = new File(fullPath);
          if (!directory.isDirectory()) {
            System.err.println(fullPath + " is not a directory");
          }
          File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File p_dir, String name) {
              return name.endsWith(".jamon");
            }
          });
          for (int i = 0; i < files.length; i++) {
            processor.generateSource(directoryName + "/" + files[i].getName());
          }
        }
        else {
          processor.generateSource(args[arg++]);
        }
      }
    }
    catch (ParserErrorsImpl e) {
      e.printErrors(System.err);
      System.exit(2);
    }
    catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
