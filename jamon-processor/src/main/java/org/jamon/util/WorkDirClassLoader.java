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
 * Contributor(s):
 */

package org.jamon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.HashMap;

public class WorkDirClassLoader extends ClassLoader {
  public WorkDirClassLoader(ClassLoader parent, String workDir) {
    super(parent);
    this.workDir = workDir;
  }

  private final String workDir;
  private final Object loaderMutex = new Object();
  private Loader loader;

  private File getFileForClass(String name) {
    return new File(workDir, StringUtils.classNameToFilePath(name) + ".class");
  }

  public void invalidate() {
    synchronized (loaderMutex) {
      loader = null;
    }
  }

  private class Loader extends ClassLoader {
    Loader() {
      super(WorkDirClassLoader.this);
    }

    @Override
    public String toString() {
      return super.toString() + " { " + " parent: " + getParent() + " }";
    }

    private final Map<String, Class<?>> cache = new HashMap<String, Class<?>>();

    private byte[] readBytesForClass(String name) throws IOException {
      FileInputStream s = new FileInputStream(getFileForClass(name));
      try {
        final byte[] buf = new byte[1024];
        byte[] bytes = new byte[0];
        while (true) {
          int i = s.read(buf);
          if (i <= 0) {
            break;
          }
          byte[] newbytes = new byte[bytes.length + i];
          System.arraycopy(bytes, 0, newbytes, 0, bytes.length);
          System.arraycopy(buf, 0, newbytes, bytes.length, i);
          bytes = newbytes;
        }
        return bytes;
      }
      finally {
        s.close();
      }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (!getFileForClass(name).exists()) {
        return super.loadClass(name, resolve);
      }
      else {
        Class<?> c = cache.get(name);
        if (c == null) {
          try {
            byte[] code = readBytesForClass(name);
            c = this.defineClass(name, code, 0, code.length);
            if (resolve) {
              this.resolveClass(c);
            }
            cache.put(name, c);
          }
          catch (IOException e) {
            throw new ClassNotFoundException(e.getMessage());
          }
        }
        return c;
      }
    }

  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve)
  throws ClassNotFoundException {
    if (!getFileForClass(name).exists()) {
      return super.loadClass(name, resolve);
    }
    else {
      if (loader == null) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
          @Override
          public Void run() {
            synchronized (loaderMutex) {
              if (loader == null) {
                loader = new Loader();
              }
            }
            return null;
          }
        });
      }
      return loader.loadClass(name, resolve);
    }
  }

  @Override
  public String toString() {
    return super.toString() + " { workDir: " + workDir + "; parent: " + getParent() + " }";
  }
}
