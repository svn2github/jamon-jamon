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
 * Contributor(s): Ian Robertson
 */

package org.jamon;

import java.io.File;
import java.io.IOException;

import org.jamon.parser.ParserException;
import org.jamon.lexer.LexerException;
import org.jamon.node.Token;

public class JamonParseException
    extends JamonTemplateException
{
    private static class Details
    {
        int line;
        int column;
        String description;
    }

    private JamonParseException(String p_fileName, Details p_details)
    {
        super(p_details.description, p_fileName,
              p_details.line, p_details.column);
    }

    public JamonParseException(String p_fileName, LexerException p_exception)
    {
        this(p_fileName, parseLexerException(p_exception));
    }

    public JamonParseException(String p_fileName, ParserException p_exception)
    {
        this(p_fileName, parseParserException(p_exception));
    }

    private static Details parseLexerException(LexerException p_exception)
    {
        Details details = new Details();
        String message = p_exception.getMessage();
        int i = message.indexOf(',');
        details.line = Integer.parseInt(message.substring(1, i));
        int j = message.indexOf(']');
        details.column = Integer.parseInt(message.substring(i+1,j));
        details.description = message.substring(j+2);
        return details;
    }

    private static Details parseParserException(ParserException p_exception)
    {
        Details details = new Details();
        Token token = p_exception.getToken();
        details.line = token.getLine();
        details.column = token.getPos();
        int i = p_exception.getMessage().lastIndexOf(']');
        details.description = p_exception.getMessage().substring(i+1);
        return details;
    }

    public String getStandardMessage()
    {
        return getFileName() + ":" + getLine() + ":" + getColumn() + ":"
            + getMessage();
    }
}


/*
  "[" + (start_line + 1) + "," + (start_pos + 1) + "]" +
  " Unknown token: " + text);
*/
