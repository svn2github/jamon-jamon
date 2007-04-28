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

import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.OptionalArgNode;

public class FragmentUnit extends AbstractInnerUnit
{
    public FragmentUnit(String p_name, StatementBlock p_parent,
                        GenericParams p_genericParams, ParserErrorsImpl p_errors, org.jamon.api.Location p_location)
    {
        super(p_name, p_parent, p_errors, p_location);
        m_genericParams = p_genericParams;
    }

    public String getFragmentInterfaceName(boolean p_makeGeneric)
    {
        String genericParamsClause = p_makeGeneric
            ? m_genericParams.generateGenericParamsList()
            : "";
        if(getParent() instanceof AbstractInnerUnit)
        {
            return "Fragment_" + getParentUnit().getName()
                + "__jamon__" + getName() + genericParamsClause;
        }
        else
        {
            return "Fragment_" + getName() + genericParamsClause;
        }
    }

    @Override
    public void addOptionalArg(OptionalArgNode p_node)
    {
        getErrors().addError(
            "Fragments cannot have optional arguments",
            p_node.getValue().getLocation());
    }

    @Override
    public void addOptionalArg(OptionalArgument p_arg)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void addFragmentArg(FragmentArgument p_arg)
    {
        getErrors().addError("Fragments cannot have fragment arguments",
                             p_arg.getLocation());
    }

    @Override
    public FragmentUnit getFragmentUnitIntf(String p_path)
    {
        return getParent().getFragmentUnitIntf(p_path);
    }

    public void printInterface(CodeWriter p_writer,
                               String p_interfaceModifiers,
                               boolean p_isCopy)
    {
        m_genericParams.suppressGenericHidingWarnings(p_writer);
        p_writer.println(p_interfaceModifiers + " static interface "
                         + getFragmentInterfaceName(true));
        if (p_isCopy)
        {
            p_writer.println("  extends Intf." + getFragmentInterfaceName(true));
        }
        p_writer.openBlock();
        if (! p_isCopy)
        {
            p_writer.print("void renderNoFlush");
            p_writer.openList();
            p_writer.printListElement(ArgNames.WRITER_DECL);
            printRenderArgsDecl(p_writer);
            p_writer.closeList();
            p_writer.println();
            p_writer.println("  throws java.io.IOException;");
            p_writer.print(ClassNames.RENDERER + " makeRenderer");
            p_writer.openList();
            printRenderArgsDecl(p_writer);
            p_writer.closeList();
            p_writer.println(";");
        }
        p_writer.closeBlock();
        p_writer.println();
    }

    private final GenericParams m_genericParams;
}
