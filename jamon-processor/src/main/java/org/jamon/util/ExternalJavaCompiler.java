/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExternalJavaCompiler implements JavaCompiler {
  private final List<String> compilerCommand;

  public ExternalJavaCompiler(String javac, List<String> compilerArgs) {
    compilerCommand = new ArrayList<String>(compilerArgs.size() + 1);
    compilerCommand.add(javac);
    compilerCommand.addAll(compilerArgs);
  }

  @Override
  public String compile(String[] javaFiles) {
    String[] cmdline = new String[javaFiles.length + compilerCommand.size()];
    compilerCommand.toArray(cmdline);
    System.arraycopy(javaFiles, 0, cmdline, compilerCommand.size(), javaFiles.length);

    Process p;
    try {
      p = Runtime.getRuntime().exec(cmdline);
    }
    catch (IOException e) {
      return e.getMessage();
    }
    StreamConsumer stderr = new StreamConsumer(p.getErrorStream());
    try {
      Thread errThread = new Thread(stderr);
      errThread.start();
      int code = -1;
      try {
        code = p.waitFor();
      }
      catch (InterruptedException e) {
        errThread.interrupt();
      }

      try {
        errThread.join();
      }
      catch (InterruptedException e) {
        // just ignore it
      }
      return code == 0
          ? null
          : stderr.getContents();
    }
    finally {
      try {
        stderr.close();
      }
      catch (IOException e) {
        return e.getMessage();
      }
    }
  }

  private static class StreamConsumer implements Runnable {
    StreamConsumer(InputStream stream) {
      this.stream = stream;
    }

    private final InputStream stream;
    private final StringBuilder buffer = new StringBuilder();

    private void close() throws IOException {
      stream.close();
    }

    synchronized String getContents() {
      return buffer.toString();
    }

    @Override
    public void run() {
      final byte[] buf = new byte[1024];
      boolean eof = false;
      while (!eof) {
        try {
          int read = stream.read(buf);
          if (read == -1) {
            eof = true;
          }
          else if (read == 0) {
            try {
              Thread.sleep(100);
            }
            catch (InterruptedException e) {
              // FIXME: really?
              eof = true;
            }
          }
          else {
            synchronized (buffer) {
              buffer.append(new String(buf, 0, read));
            }
          }
        }
        catch (IOException e) {
          // FIXME: what here?
          eof = true;
        }
      }
    }
  }

}
