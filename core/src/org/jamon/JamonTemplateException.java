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

import org.jamon.parser.ParserException;
import org.jamon.lexer.LexerException;
import org.jamon.node.Token;

public abstract class JamonTemplateException
    extends JamonRuntimeException
{
    public JamonTemplateException(String p_message,
                                  String p_fileName,
                                  int p_line,
                                  int p_column)
    {
        super(p_message);
        m_fileName = p_fileName;
        m_line = p_line;
        m_column = p_column;
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
}
