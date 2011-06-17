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
 * Contributor(s): Luis O'Shea
 */

package org.jamon.codegen;

import org.jamon.emit.EmitMode;

public class WriteStatement
    extends AbstractStatement
{
    WriteStatement(String p_expr,
                   EscapingDirective p_escapingDirective,
                   org.jamon.api.Location p_location,
                   String p_templateIdentifier,
                   EmitMode p_emitMode)
    {
        super(p_location, p_templateIdentifier);
        m_expr = p_expr.trim();
        m_escapingDirective = p_escapingDirective;
        m_emitMode = p_emitMode;
    }

    @Override
    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer)
    {
        if (! "\"\"".equals(m_expr))
        {
            generateSourceLine(p_writer);
            p_writer.println(m_escapingDirective.toJava() + ".write("
                             + m_emitMode.getEmitterClassName()
                             + ".valueOf(" + m_expr + ")"
                             + ", " + ArgNames.WRITER + ");");
        }
    }

    private final String m_expr;
    private final EscapingDirective m_escapingDirective;
    private final EmitMode m_emitMode;
}
