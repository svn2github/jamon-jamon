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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jamon.node.TIdentifier;
import org.jamon.node.AArg;
import org.jamon.node.ADefault;

public class FragmentUnit extends AbstractInnerUnit
{
    public FragmentUnit(String p_name, Unit p_parent)
    {
        super(p_name, p_parent);
    }

    public String getFragmentInterfaceName()
    {
        if(getParent() instanceof AbstractInnerUnit)
        {
            return "Fragment_" + getParent().getName()
                + "__jamon__" + getName();
        }
        else
        {
            return "Fragment_" + getName();
        }
    }

    public FragmentUnit addFragment(TIdentifier p_fragName)
    {
        throw new TunnelingException(
            "Fragments cannot have fragment arguments",
            p_fragName);
    }

    public void addNonFragmentArg(AArg p_arg)
    {
        if (p_arg.getDefault() != null)
        {
            throw new TunnelingException(
                "Fragments cannot have optional arguments",
                ((ADefault) p_arg.getDefault()).getArrow());
        }
        else
        {
            super.addNonFragmentArg(p_arg);
        }
    }

    public void addOptionalArg(OptionalArgument p_arg)
    {
        throw new UnsupportedOperationException();
    }

    protected void addFragmentArg(FragmentArgument p_arg)
    {
        throw new UnsupportedOperationException();
    }

    public FragmentUnit getFragmentUnitIntf(String p_path)
    {
        return getParent().getFragmentUnitIntf(p_path);
    }

    public void printInterface(IndentingWriter p_writer,
                               String p_interfaceModifiers,
                               boolean p_isCopy)
    {
        p_writer.println(p_interfaceModifiers + " static interface "
                         + getFragmentInterfaceName());
        p_writer.println("  extends "
                         + (p_isCopy
                            ? ("Intf." + getFragmentInterfaceName())
                            : ClassNames.FRAGMENT_INTF));
        p_writer.openBlock();
        if (! p_isCopy)
        {
            p_writer.print  ("void render(");
            printRenderArgsDecl(p_writer);
            p_writer.println(")");
            p_writer.println("  throws java.io.IOException;");
            p_writer.print(ClassNames.RENDERER + " makeRenderer(");
            printRenderArgsDecl(p_writer);
            p_writer.println(");");
        }
        p_writer.closeBlock();
        p_writer.println();
    }
}
