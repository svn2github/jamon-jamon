/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;

/**
 * Converts a string into an escaped version of it. <em>Escaping</em> is the process of converting a
 * string into another string such that
 * <ul>
 * <li>the process can be inverted (i.e., you can recover the original string from its escaped
 * version), and
 * <li>the escaped version is gauranteed to satisfy certain constraints
 * </ul>
 * Typically, the constraints are that a certain set of characters cannot appear in the escaped
 * string.
 * <p>
 * Examples of escaping mechanisms are HTML escaping, URL escaping.
 **/

public interface Escaping {
  void write(String string, Writer writer) throws IOException;

  /**
   * An escaping mechanism which escapes suitable for inclusion in HTML documents.
   **/
  Escaping HTML = new HtmlEscaping();

  /**
   * An escaping mechanism which escapes suitable for inclusion inside html attributes.
   **/
  Escaping STRICT_HTML = new StrictHtmlEscaping();

  /**
   * An escaping mechanism which passes through strings without change.
   **/
  Escaping NONE = new NoneEscaping();

  /**
   * An escaping mechanism which escapes suitable for inclusion in URLs.
   **/
  Escaping URL = new UrlEscaping();

  /**
   * An escaping mechanism which escapes suitable for inclusion in XML documents.
   **/
  Escaping XML = new XmlEscaping();

  /**
   * An escaping mechanism which escapes suitable for inclusion in in Javascript. Note that it does
   * not do any HTML escaping.
   **/
  Escaping JAVASCRIPT = new JavascriptEscaping();
}
