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
 * The Initial Developer of the Original Code is Luis O'Shea.  Portions
 * created by Luis O'Shea are Copyright (C) 2003 Luis O'Shea.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;
import java.net.URLEncoder;

public class UrlEscaping
    implements Escaping
{
    static boolean passThru[] = new boolean[0x80];
    static
    {
        for(char c = 'A'; c <= 'Z'; c++) { passThru[c] = true; }
        for(char c = 'a'; c <= 'z'; c++) { passThru[c] = true; }
        for(char c = 'a'; c <= 'z'; c++) { passThru[c] = true; }
        for(char c = '0'; c <= '9'; c++) { passThru[c] = true; }
        passThru['.'] = passThru['-'] = passThru['*'] = passThru['_'] = true;
    }

    UrlEscaping()
    {
        // package scope constructor
    }

    public void write(String p_string, Writer p_writer)
        throws IOException
    {
        // In a java 1.4 world, we could just call
        // p_writer.write(URLEncoder.encode(p_string, "UTF-8"));
        // However this is not available in jdk 1.3, and a call to
        // just URLEncoder(p_string) is justly deprecated in java 1.4.
        // To preserve java 1.3 compatibility, we mimic the important
        // parts of URLEncoder here.

        for (int i = 0; i < p_string.length(); i++)
        {
            char c = p_string.charAt(i);
            if(c < 0x80)
            {
                if(passThru[c])
                {
                    p_writer.write(c);
                }
                else if(c == ' ')
                {
                    p_writer.write("+");
                }
                else
                {
                    percentEscape(p_writer, (char) c);
                }
            }
            else if (c < 0xD800 || c >= 0xE000)
            {
                escape(p_writer, c);
            }
            else if (c >= 0xDC00)
            {
                escapeJunk(p_writer);
            }
            else if (i+1 < p_string.length())
            {
                char d = p_string.charAt(i+1);
                if(d >= 0xDC00 && d < 0xE000)
                {
                    i++; // it's unicode UTF-16 funkiness.
                    escape(p_writer,
                           ((long) (((c >> 6) & 0xF) + 1)) << 16
                           | ((long) (c & 0x3F)) << 10
                           | d & 0x3FF);
                }
                else
                {
                    escapeJunk(p_writer);
                }
            }
            else
            {
                // Match behavior of URLEncoder and do nothing
            }
        }
    }

    static private void escapeJunk(Writer p_writer)
        throws IOException
    {
        //Match the behavior of URLEncoder for malformed UTF-16 strings
        percentEscape(p_writer, '?');
    }

    static private void escape(Writer p_writer, long p_unicode)
        throws IOException
    {
        // We assume p_unicode >= 0x80
        if (p_unicode < 0x800)
        {
            percentEscape(p_writer, (char) (0xC0 | p_unicode >> 6));
            escapeRest(p_writer, p_unicode, 6);
        }
        else if (p_unicode < 0x10000)
        {
            percentEscape(p_writer, (char) (0xE0 | p_unicode >> 12));
            escapeRest(p_writer, p_unicode, 12);
        }
        else if (p_unicode < 0x200000)
        {
            percentEscape(p_writer, (char) (0xF0 | p_unicode >> 20));
            escapeRest(p_writer, p_unicode, 18);
        }
        else if (p_unicode < 0x4000000)
        {
            percentEscape(p_writer, (char) (0xF8 | p_unicode >> 24));
            escapeRest(p_writer, p_unicode, 24);
        }
        else // we may assume  p_unicode < 0x80000000
        {
            percentEscape(p_writer, (char) (0xFC | p_unicode >> 30));
            escapeRest(p_writer, p_unicode, 30);
        }
    }

    static private void escapeRest(Writer p_writer, long  p_long, int p_bits)
        throws IOException
    {
        while(p_bits > 0)
        {
            percentEscape(p_writer,
                          (char) (0x80 | 0x3F & (p_long >> (p_bits -= 6))));
        }
    }

    final static char hexChars[] = "0123456789ABCDEF".toCharArray();

    // See RFC 2481
    static private void percentEscape(Writer p_writer, char p_char)
        throws IOException
    {
        p_writer.write("%");
        p_writer.write(hexChars[p_char >> 4]);
        p_writer.write(hexChars[p_char & 0xF]);
    }
}
