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

import org.jamon.node.Token;
import org.jamon.emit.EmitMode;

public class RawStatement
    extends AbstractStatement
{
    RawStatement(String p_code,
                 Token p_token,
                 String p_templateIdentifier)
    {
        super(p_token, p_templateIdentifier);
        m_code = p_code;
    }

    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer,
                               EmitMode p_emitMode)
    {
        generateSourceLine(p_writer);
        p_writer.println(m_code);
    }

    private final String m_code;
}
