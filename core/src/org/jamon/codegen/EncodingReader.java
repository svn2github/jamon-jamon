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
        PushbackInputStream stream = new PushbackInputStream(p_stream, 50);
        if (matches(ONEBYTESIG, stream))
        {
            m_reader = new InputStreamReader(stream,
                                             computeOneByteEncoding(stream));
        }
        else if (matches(UTF16SIG, stream))
        {
            m_reader = new InputStreamReader(stream,
                                             computeUtf16Encoding(stream));
        }
        else
        {
            m_reader = new InputStreamReader(stream);
        }
    }

    private boolean matches(byte[] match, PushbackInputStream p_stream)
        throws IOException
    {
        byte[] data = new byte[match.length];
        int len = p_stream.read(data);
        if (len == match.length)
        {
            for (int i = 0; i < len; ++i)
            {
                if (match[i] != data[i])
                {
                    p_stream.unread(data, 0, len);
                    return false;
                }

            }
            return true;
        }
        p_stream.unread(data, 0, len);
        return false;
    }

    private String computeUtf16Encoding(InputStream p_stream)
        throws IOException
    {
        return computeEncoding(p_stream, true);
    }

    private String computeOneByteEncoding(InputStream p_stream)
        throws IOException
    {
        return computeEncoding(p_stream, false);
    }

    private String computeEncoding(InputStream p_stream, boolean p_twoBytes)
        throws IOException
    {
        final int SPACE = (int) ' ';
        final int TAB = (int) '\t';
        final int CLOSE = (int) '>';

        final int START = 0;
        final int INNAME = 1;
        final int WAITFORCLOSE = 2;

        StringBuffer encoding = new StringBuffer();
        boolean lowByte = true;
        int state = 0;
        while (true)
        {
            int c = p_stream.read();
            if (p_twoBytes)
            {
                if (lowByte)
                {
                    if (c != 0)
                    {
                    }
                    lowByte = false;
                    continue;
                }
                else
                {
                    lowByte = true;
                }
            }

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
                encoding.append((char)c);
            }
            else
            {
                throw new IOException("Malformed encoding tag");
            }
        }
        System.err.println("Encoding is " + encoding);
        return encoding.toString();
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
            UTF16SIG = "<%encoding ".getBytes("UTF-16");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            throw new JamonRuntimeException(e);
        }
    }
}
