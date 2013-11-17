/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
