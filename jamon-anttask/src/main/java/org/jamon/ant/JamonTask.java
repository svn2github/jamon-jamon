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
 * Contributor(s): Luis O'Shea, Ian Robertson
 */

package org.jamon.ant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;

import org.apache.tools.ant.types.Path;

import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.SourceFileScanner;

import org.apache.tools.ant.taskdefs.MatchingTask;

import org.jamon.api.ParserError;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.compiler.TemplateProcessor;

/**
 * Ant task to convert Jamon templates into Java.
 **/

public class JamonTask extends MatchingTask {

  public void setDestdir(File destDir) {
    this.destDir = destDir;
  }

  public void setSrcdir(File srcDir) {
    this.srcDir = srcDir;
  }

  public void setClasspath(Path classpath) throws IOException {
    String[] paths = classpath.list();
    URL[] urls = new URL[paths.length];
    for (int i = 0; i < urls.length; ++i) {
      urls[i] = new URL("file", null, paths[i]);
    }
    classLoader = AccessController.doPrivileged(new ClassLoaderCreator(urls));
  }

  private static class ClassLoaderCreator implements PrivilegedAction<ClassLoader> {
    private final URL[] urls;

    ClassLoaderCreator(URL[] urls) {
      this.urls = urls;
    }

    public ClassLoader run() {
      return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
    }
  }

  public void setListFiles(boolean listFiles) {
    this.listFiles = listFiles;
  }

  @Override
  public void execute() throws BuildException {
    // Copied from org.apache.tools.ant.taskdefs.Javac below

    // first off, make sure that we've got a srcdir

    if (srcDir == null) {
      throw new BuildException("srcdir attribute must be set!", getLocation());
    }
    if (destDir == null) {
      throw new BuildException("destdir attribute must be set!", getLocation());
    }

    if (!srcDir.exists() && !srcDir.isDirectory()) {
      throw new BuildException(
        "source directory \"" + srcDir + "\" does not exist or is not a directory", getLocation());
    }

    destDir.mkdirs();
    if (!destDir.exists() || !destDir.isDirectory()) {
      throw new BuildException(
        "destination directory \"" + destDir + "\" does not exist or is not a directory",
        getLocation());
    }

    if (!srcDir.exists()) {
      throw new BuildException("srcdir \"" + srcDir + "\" does not exist!", getLocation());
    }

    SourceFileScanner sfs = new SourceFileScanner(this);
    File[] files = sfs.restrictAsFiles(
      getDirectoryScanner(srcDir).getIncludedFiles(), srcDir, destDir, new JamonFileNameMapper());

    if (files.length > 0) {
      log("Processing " + files.length + " template" + (files.length == 1
          ? ""
          : "s") + " to " + destDir);

      TemplateProcessor processor = new TemplateProcessor(destDir, srcDir, classLoader);

      for (int i = 0; i < files.length; i++) {
        if (listFiles) {
          log(files[i].getAbsolutePath());
        }

        try {
          processor.generateSource(relativize(files[i]));
        }
        catch (ParserErrorsImpl e) {
          // FIXME - is this the right thing to do?
          e.printErrors(System.err);
          if (!e.getErrors().isEmpty()) {
            ParserError error = e.getErrors().get(0);
            throw new BuildException(error.getMessage(), new JamonLocation(error.getLocation()));
          }
          else {
            throw new BuildException("Jamon translation failed");
          }
        }
        catch (Exception e) {
          throw new BuildException(
            e.getClass().getName() + ":" + e.getMessage(),
            new Location(files[i].getAbsoluteFile().toString()));
        }
      }
    }
  }

  private static class JamonFileNameMapper implements FileNameMapper {
    public void setFrom(String from) {}

    public void setTo(String to) {}

    public String[] mapFileName(String sourceName) {
      String targetFileName = sourceName;
      int i = targetFileName.lastIndexOf('.');
      if (i > 0 && "jamon".equals(targetFileName.substring(i + 1))) {
        targetFileName = targetFileName.substring(0, i);
        return new String[] { targetFileName + ".java", targetFileName + "Impl.java" };
      }
      return new String[0];
    }
  }

  private String relativize(File file) {
    if (!file.isAbsolute()) {
      throw new IllegalArgumentException("Paths must be all absolute");
    }
    String filePath = file.getPath();
    String basePath = srcDir.getAbsoluteFile().toString(); // FIXME !?

    if (filePath.startsWith(basePath)) {
      return filePath.substring(basePath.length() + 1);
    }
    else {
      throw new IllegalArgumentException(file + " is not based at " + basePath);
    }
  }

  private File destDir = null;
  private File srcDir = null;
  private boolean listFiles = false;
  private ClassLoader classLoader = JamonTask.class.getClassLoader();
}
