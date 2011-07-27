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

package org.jamon.codegen;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class EncodingReader extends Reader {
  public static class Exception extends IOException {
    private static final long serialVersionUID = 2006091701L;

    public Exception(String message, int pos) {
      super(message);
      this.pos = pos;
    }

    public int getPos() {
      return pos;
    }

    private final int pos;
  }

  public EncodingReader(InputStream stream) throws IOException {
    PushbackInputStream pushbackStream = new PushbackInputStream(stream, 50);
    if (matches(ONEBYTESIG, pushbackStream)) {
      encoding = computeOneByteEncoding(pushbackStream);
    }
    else if (matches(UTF16LESIG, pushbackStream) || matches(UTF16BESIG, pushbackStream)) {
      encoding = computeUtf16Encoding(pushbackStream);
    }
    else {
      encoding = Charset.defaultCharset().name();
    }
    reader = new InputStreamReader(pushbackStream, encoding);
  }

  public String getEncoding() {
    return encoding;
  }

  private boolean matches(byte[] match, PushbackInputStream stream) throws IOException {
    byte[] data = new byte[match.length];
    int len = stream.read(data);
    if (len == -1) {
      return false;
    }
    if (len == match.length) {
      for (int i = 0; i < len; ++i) {
        if (match[i] != data[i]) {
          stream.unread(data, 0, len);
          return false;
        }

      }
      bytesRead = len;
      return true;
    }
    stream.unread(data, 0, len);
    return false;
  }

  private String computeUtf16Encoding(PushbackInputStream stream) throws IOException {
    return computeEncoding(stream, true);
  }

  private String computeOneByteEncoding(PushbackInputStream stream) throws IOException {
    return computeEncoding(stream, false);
  }

  private String computeEncoding(PushbackInputStream stream, boolean twoBytes)
  throws IOException {

    StringBuilder encoding = new StringBuilder();
    boolean lowByte = true;
    int state = 0;
    while (true) {
      int c = stream.read();
      bytesRead++;
      if (twoBytes) {
        if (lowByte) {
          if (c != 0) {
            throw new Exception("Malformed encoding name", bytesRead / (twoBytes
                ? 2
                : 1));
          }
          lowByte = false;
          continue;
        }
        else {
          lowByte = true;
        }
      }

      if (c == -1) {
        throw new Exception("EOF before encoding tag finished", bytesRead / (twoBytes
            ? 2
            : 1));
      }
      else if (c == SPACE || c == TAB) {
        if (state == INNAME) {
          state = WAITFORCLOSE;
        }
      }
      else if (c == CLOSE) {
        state = CLOSED;
      }
      else if (state == CLOSED) {
        if (c != '\r' && c != '\n') {
          stream.unread(c);
          if (twoBytes) {
            stream.unread(0);
          }
          break;
        }
      }
      else if (state != WAITFORCLOSE) {
        state = INNAME;
        encoding.append((char) c);
      }
      else {
        throw new Exception("Malformed encoding tag; expected '>'", bytesRead / (twoBytes
            ? 2
            : 1));
      }
    }
    return encoding.toString();
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  @Override
  public int read(char[] buf, int offset, int len) throws IOException {
    return reader.read(buf, offset, len);
  }

  private final Reader reader;
  private String encoding;
  private int bytesRead;

  private static final byte[] ONEBYTESIG;
  private static final byte[] UTF16LESIG;
  private static final byte[] UTF16BESIG;

  static {
    try {
      ONEBYTESIG = "<%encoding ".getBytes("latin1");
      UTF16BESIG = "<%encoding ".getBytes("UTF-16BE");
      UTF16LESIG = "<%encoding ".getBytes("UTF-16LE");
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private final static int SPACE = ' ';
  private final static int TAB = '\t';
  private final static int CLOSE = '>';
  private final static int INNAME = 1;
  private final static int WAITFORCLOSE = 2;
  private final static int CLOSED = 3;
}
