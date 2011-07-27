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
 * Contributor(s):
 */

package org.jamon.codegen;

import java.util.Map;
import java.util.HashMap;

public class EscapingDirective {
  public String toJava() {
    return java;
  }

  public static final String DEFAULT_ESCAPE_CODE = "h";

  public static EscapingDirective get(String abbreviation) {
    return standardDirectives.get(abbreviation);
  }

  private EscapingDirective(String java) {
    this.java = PREFIX + java;
  }

  private final String java;

  private static final Map<String, EscapingDirective> standardDirectives =
    new HashMap<String, EscapingDirective>();

  private static final String PREFIX = org.jamon.escaping.Escaping.class.getName() + ".";

  static {
    standardDirectives.put("H", new EscapingDirective("STRICT_HTML"));
    standardDirectives.put(DEFAULT_ESCAPE_CODE, new EscapingDirective("HTML"));
    standardDirectives.put("n", new EscapingDirective("NONE"));
    standardDirectives.put("u", new EscapingDirective("URL"));
    standardDirectives.put("x", new EscapingDirective("XML"));
    standardDirectives.put("j", new EscapingDirective("JAVASCRIPT"));
  }
}
