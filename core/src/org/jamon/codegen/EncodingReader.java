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

import org.jamon.JamonRuntimeException;

public class EncodingReader
    extends Reader
{
    public EncodingReader(InputStream p_stream)
        throws IOException
    {
        PushbackInputStream stream = new PushbackInputStream(p_stream, 30);
        byte[] data = new byte[ONEBYTESIG.length];
        int len = stream.read(data);
        if (len != data.length && len > 0)
        {
            stream.unread(data, 0, len);
            m_reader = new InputStreamReader(stream);
        }
        else
        {
            int c;
            final int SPACE = (int) ' ';
            final int TAB = (int) '\t';
            final int CLOSE = (int) '>';

            final int START = 0;
            final int INNAME = 1;
            final int WAITFORCLOSE = 2;

            StringBuffer encoding = new StringBuffer();

            int state = 0;
            while (true)
            {
                c = stream.read();
                if (c == -1)
                {
                    throw new IOException("EOF before encoding tag finished");
                }
                else if (c == SPACE || c == TAB)
                {
                    if (state == INNAME)
                    {
                        state = WAITFORCLOSE;
                    }
                }
                else if (c == CLOSE)
                {
                    break;
                }
                else if (state != WAITFORCLOSE)
                {
                    state = INNAME;
                    // FIXME: check that c is ASCII
                    encoding.append((char)c);
                }
                else
                {
                    throw new IOException("Malformed encoding tag");
                }
            }
            m_reader = new InputStreamReader(stream, encoding.toString());
        }
    }

    public void close()
        throws IOException
    {
        m_reader.close();
    }

    public int read(char[] p_buf, int p_offset, int p_len)
        throws IOException
    {
        return m_reader.read(p_buf, p_offset, p_len);
    }

    private final Reader m_reader;

    private static final byte[] ONEBYTESIG;
    private static final byte[] UTF16SIG;
    static
    {
        try
        {
            ONEBYTESIG = "<%encoding ".getBytes("latin1");
            UTF16SIG = "<%encoding ".getBytes("utf16");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new JamonRuntimeException(e);
        }
    }
}
