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
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon;

import java.io.File;
import java.io.IOException;

import org.jamon.parser.ParserException;
import org.jamon.lexer.LexerException;
import org.jamon.node.Token;

public class JamonParseException
    extends IOException
{
    public JamonParseException(File p_file, LexerException p_exception)
    {
        super(p_exception.getMessage());
        m_fileName = p_file.getAbsolutePath();
        String message = p_exception.getMessage();
        int i = message.indexOf(',');
        m_line = Integer.parseInt(message.substring(1, i));
        int j = message.indexOf(']');
        m_column = Integer.parseInt(message.substring(i+1,j));
        m_description = message.substring(j+2);
    }

    public JamonParseException(File p_file, ParserException p_exception)
    {
        super(p_exception.getMessage());
        m_fileName = p_file.getAbsolutePath();
        Token token = p_exception.getToken();
        m_line = token.getLine();
        m_column = token.getPos();
        int i = p_exception.getMessage().lastIndexOf(']');
        m_description = p_exception.getMessage().substring(i+1);
    }

    public String getStandardMessage()
    {
        return getFileName() + ":" + getLine() + ":" + getColumn() + ":"
            + getDescription();
    }

    public String getDescription()
    {
        return m_description;
    }

    public String getFileName()
    {
        return m_fileName;
    }

    public int getLine()
    {
        return m_line;
    }

    public int getColumn()
    {
        return m_column;
    }

    private final int m_line;
    private final int m_column;
    private final String m_fileName;
    private final String m_description;
}


/*
  "[" + (start_line + 1) + "," + (start_pos + 1) + "]" +
  " Unknown token: " + text);
*/
