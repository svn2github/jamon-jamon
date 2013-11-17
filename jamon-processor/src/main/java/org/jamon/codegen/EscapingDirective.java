/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
