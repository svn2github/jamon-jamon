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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */
package org.jamon.parser;

import org.jamon.compiler.ParserErrorImpl;


class OptionalValueTagEndDetector implements TagEndDetector
{
    public static final String NEED_SEMI_OR_ARROW =
        "Expecting a ';', '=' or '=>'";

    public int checkEnd(char p_char)
       { return p_char == ';' ? 1 : 0; }

    public ParserErrorImpl getEofError(org.jamon.api.Location p_startLocation)
    {
        return new ParserErrorImpl(p_startLocation, ArgsParser.EOF_LOOKING_FOR_SEMI);
    }

    public void resetEndMatch() {}
}