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

public class EscapingDirective
{
    public String toJava()
    {
        return m_java;
    }

    public static final String DEFAULT_ESCAPE_CODE = "h";

    public static EscapingDirective get(String p_abbreviation)
    {
        return (EscapingDirective) s_standardDirectives.get(p_abbreviation);
    }


    private EscapingDirective(String p_java)
    {
        m_java = PREFIX + p_java;
    }

    private final String m_java;

    private static final Map s_standardDirectives = new HashMap();

    private static final String PREFIX =
        org.jamon.escaping.Escaping.class.getName() + ".";

    static
    {
        s_standardDirectives.put("H",
                                 new EscapingDirective("STRICT_HTML"));
        s_standardDirectives.put(DEFAULT_ESCAPE_CODE,
                                 new EscapingDirective("HTML"));
        s_standardDirectives.put("n",
                                 new EscapingDirective("NONE"));
        s_standardDirectives.put("u",
                                 new EscapingDirective("URL"));
        s_standardDirectives.put("x",
                                 new EscapingDirective("XML"));
        s_standardDirectives.put("j",
                                 new EscapingDirective("JAVASCRIPT"));
    }
}
