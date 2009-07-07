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

import org.jamon.util.StringUtils;

public class LiteralStatement
    extends AbstractStatement
{
    LiteralStatement(
        String p_text, org.jamon.api.Location p_token, String p_templateIdentifier)
    {
        super(p_token, p_templateIdentifier);
        m_text = new StringBuilder(p_text);
    }

    public void appendText(String p_text)
    {
        m_text.append(p_text);
    }

    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer)
    {
        if (m_text.length() > 0)
        {
            generateSourceLine(p_writer);
            p_writer.print(ArgNames.WRITER + ".write(\"");
            javaEscape(m_text.toString(), p_writer);
            p_writer.println("\");");
        }
    }

    private void javaEscape(String p_string, CodeWriter p_writer)
    {
        // assert p_string != null
        for (int i = 0; i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            switch(c)
            {
              case '\\': p_writer.print("\\\\"); break;
              case '\n': p_writer.print("\\n"); break;
              case '\r': p_writer.print("\\r"); break;
              case '\t': p_writer.print("\\t"); break;
              case '\"': p_writer.print("\\\""); break;
              default:
                  {
                      int ci = c;
                      if (ci < 32 || ci > 127)
                      {
                          p_writer.print("\\u");
                          p_writer.print(StringUtils.hexify4(ci));
                      }
                      else
                      {
                          p_writer.print(c);
                      }
                  }
            }
        }
    }

    public String getText()
    {
        return m_text.toString();
    }

    private final StringBuilder m_text;
}
