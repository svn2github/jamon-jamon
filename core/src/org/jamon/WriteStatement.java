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

import java.io.PrintWriter;
import java.io.IOException;

public class WriteStatement
    implements Statement
{
    WriteStatement(String p_expr, Encoding p_encoding)
    {
        m_expr = p_expr;
        m_encoding = p_encoding;
    }

    public void generateSource(PrintWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
        throws IOException
    {
        p_writer.println("this.write"
                         + m_encoding
                         + "Escaped(this.valueOf("
                         + m_expr
                         + "));");
    }

    private final String m_expr;
    private final Encoding m_encoding;
}
