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

public class LiteralStatement extends AbstractStatement {
  LiteralStatement(String text, org.jamon.api.Location token, String templateIdentifier) {
    super(token, templateIdentifier);
    this.text = new StringBuilder(text);
  }

  public void appendText(String text) {
    this.text.append(text);
  }

  @Override
  public void generateSource(CodeWriter writer, TemplateDescriber describer) {
    if (text.length() > 0) {
      generateSourceLine(writer);
      writer.print(ArgNames.WRITER + ".write(\"");
      javaEscape(text.toString(), writer);
      writer.println("\");");
    }
  }

  private void javaEscape(String string, CodeWriter writer) {
    // assert string != null
    for (int i = 0; i < string.length(); ++i) {
      char c = string.charAt(i);
      switch (c) {
        case '\\':
          writer.print("\\\\");
          break;
        case '\n':
          writer.print("\\n");
          break;
        case '\r':
          writer.print("\\r");
          break;
        case '\t':
          writer.print("\\t");
          break;
        case '\"':
          writer.print("\\\"");
          break;
        default: {
          int ci = c;
          if (ci < 32 || ci > 127) {
            writer.print("\\u");
            writer.print(StringUtils.hexify4(ci));
          }
          else {
            writer.print(c);
          }
        }
      }
    }
  }

  public String getText() {
    return text.toString();
  }

  private final StringBuilder text;
}
