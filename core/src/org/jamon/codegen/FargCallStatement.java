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
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;

public class FargCallStatement
    extends AbstractCallStatement
{
    FargCallStatement(String p_path,
                      ParamValues p_params,
                      FragmentUnit p_fragmentUnit,
                      org.jamon.api.Location p_location,
                      String p_templateIdentifier)
    {
        super(p_path, p_params, p_location, p_templateIdentifier);
        m_fragmentUnit = p_fragmentUnit;
    }

    private final FragmentUnit m_fragmentUnit;

    @Override
    public void addFragmentImpl(FragmentUnit p_unit, ParserErrorsImpl p_errors)
    {
        p_errors.addError("Fragment args for fragments not implemented",
                          getLocation());
    }

    @Override
    protected String getFragmentIntfName(FragmentUnit p_fragmentUnitIntf)
    {
        throw new UnsupportedOperationException();
    }

    public void generateSource(CodeWriter p_writer,
                               TemplateDescriber p_describer) throws ParserErrorImpl
    {
        generateSourceLine(p_writer);
        String tn = getPath();
        p_writer.print  (tn + ".renderNoFlush");
        p_writer.openList();
        p_writer.printListElement(ArgNames.WRITER);
        getParams().generateRequiredArgs(m_fragmentUnit.getRequiredArgs(),
                                         p_writer);
        p_writer.closeList();
        p_writer.println(";");
        checkSuppliedParams();
    }
}
