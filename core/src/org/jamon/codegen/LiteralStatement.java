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

import org.jamon.node.Token;
import org.jamon.util.StringUtils;

public class LiteralStatement
    extends AbstractStatement
    implements Statement
{
    LiteralStatement(String p_text, Token p_token, String p_templateIdentifier)
    {
        super(p_token, p_templateIdentifier);
        m_text = javaEscape(p_text);
    }

    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer)
    {
        if (m_text.length() > 0)
        {
            generateSourceLine(p_writer);
            p_writer.print("this.write(\"");
            p_writer.print(m_text);
            p_writer.println("\");");
        }
    }

    private static String javaEscape(String p_string)
    {
        // assert p_string != null
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            switch(c)
            {
              case '\\': s.append("\\\\"); break;
              case '\n': s.append("\\n"); break;
              case '\r': s.append("\\r"); break;
              case '\t': s.append("\\t"); break;
              case '\"': s.append("\\\""); break;
              default:
                  {
                      int ci = (int) c;
                      if (ci < 32 || ci > 127)
                      {
                          s.append("\\u");
                          s.append(StringUtils.hexify4(ci));
                      }
                      else
                      {
                          s.append(c);
                      }
                  }
            }
        }
        return s.toString();
    }

    private final String m_text;
}
