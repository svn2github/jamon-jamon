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

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;

/**
 * Converts a string into an escaped version of it.  <em>Escaping</em> is the
 * process of converting a string into another string such that
 * <ul>
 *   <li>the process can be inverted (i.e., you can recover the original string
 *     from its escaped version), and
 *   <li>the escaped version is gauranteed to satisfy certain constraints
 * </ul>
 * Typically, the constraints are that a certain set of characters cannot appear
 * in the escaped string.
 * <p>
 * Examples of escaping mechanisms are HTML escaping, URL escaping.
 */

public interface Escaping
{

    void write(String p_string, Writer p_writer)
        throws IOException;

    public static final Escaping HTML = new HtmlEscaping();
    public static final Escaping STRICT_HTML = new StrictHtmlEscaping();
    public static final Escaping NONE = new NoneEscaping();
    public static final Escaping URL = new UrlEscaping();
    public static final Escaping XML = new XmlEscaping();
    public static final Escaping DEFAULT = HTML;

}
