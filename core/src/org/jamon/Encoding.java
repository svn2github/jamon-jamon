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

/**
 * An enumerated type class representing various encodings.
 */

public class Encoding
{
    /** The default encoding */
    public final static Encoding DEFAULT = new Encoding("");

    /** The "unencoded" encoding */
    public final static Encoding NONE = new Encoding("Un");

    /** HTML escape encoding */
    public final static Encoding HTML = new Encoding("Html");

    /** XML escape encoding */
    public final static Encoding XML = new Encoding("Xml");

    /** URL encoding */
    public final static Encoding URL = new Encoding("Url");



    /**
     * Convert the <code>Encoding</code> to a String.  For pure
     * convenience, these correspond directly to pieces of method
     * names in @{link AbstractTemplateImpl}.
     *
     * @return a String representation of this <code>Encoding</code>
     */
    public String toString()
    {
        return m_name;
    }

    /**
     * Standard equality definition for enumerations: identity.
     *
     * @param p_obj the object to which to compare this object
     *
     * @return true if the parameter is identical to this object,
     * false otherwise
     */
    public boolean equals(Object p_obj)
    {
        return p_obj == this;
    }

    /**
     * Construct an <code>Encoding</code>. Private, since this is an
     * enumerated type.
     *
     * @param p_name the String representation of this encoding. Need
     * not be unique.
     */
    private Encoding(String p_name)
    {
        m_name = p_name;
    }


    private final String m_name;
}
