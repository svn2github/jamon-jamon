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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Luis O'Shea.  Portions
 * created by Luis O'Shea are Copyright (C) 2002 Luis O'Shea.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.doc.diff;

import java.util.List;
import java.util.LinkedList;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

public class TokenUtils
{

    public static Token[] fineTokenize(String p_text)
    {
        char[] chars = p_text.toCharArray();
        StringBuffer whitespace = new StringBuffer();
        boolean haveNewline = true; // so we classify initial whitespace as a StringToken
        List tokenList = new LinkedList();
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if (Character.isWhitespace(c))
            {
                whitespace.append(c);
                haveNewline |= (c == '\n');
            }
            else
            {
                if (haveNewline)
                {
                    tokenList.add(new StringToken(whitespace.toString()));
                    haveNewline = false;
                }
                else
                {
                    for (int j = 0; j < whitespace.length(); j++)
                    {
                        tokenList.add(new CharacterToken(whitespace.charAt(j)));
                    }
                }
                whitespace = new StringBuffer();
                tokenList.add(new CharacterToken(c));
            }
        }
        tokenList.add(new StringToken(whitespace.toString()));
        return (Token[]) tokenList.toArray(new Token[0]);
    }


    public static Token[] coarseTokenize(String p_text)
    {
        BufferedReader reader = new BufferedReader(new StringReader(p_text));
        List tokenList = new LinkedList();
        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                tokenList.add(new LineToken(line));
            }
        }
        catch (IOException e)
        {
            // This can't actually happen
            throw new RuntimeException("readLine threw an exception on a buffered StringReader");
        }
        return (Token[]) tokenList.toArray(new Token[0]);
    }

}
