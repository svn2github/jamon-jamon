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

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;

import org.jamon.util.StringUtils;

public class ImplGenerator
{
    public ImplGenerator(Writer p_writer,
                         TemplateResolver p_resolver,
                         TemplateDescriber p_describer,
                         TemplateUnit p_templateUnit)
    {
        m_writer = new IndentingWriter(p_writer);
        m_resolver = p_resolver;
        m_describer = p_describer;
        m_templateUnit = p_templateUnit;
    }

    public void generateSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateConstructor();
        generateInitialize();
        generateRender();
        generateOptionalArgs();
        generateDefs();
        generateEpilogue();
        m_writer.finish();
    }

    private final TemplateResolver m_resolver;
    private final IndentingWriter m_writer;
    private final TemplateDescriber m_describer;
    private final TemplateUnit m_templateUnit;

    private final String getPath()
    {
        return m_templateUnit.getName();
    }

    private String getClassName()
    {
        return m_resolver.getImplClassName(getPath());
    }

    private void generateDeclaration()
        throws IOException
    {
        m_writer.print("public");
        if(m_templateUnit.isParent())
        {
            m_writer.print(" abstract");
        }
        m_writer.println(" class " + getClassName());
        m_writer.println("  extends "
                         + (m_templateUnit.hasParentPath()
                            ? m_resolver.getFullyQualifiedImplClassName(
                                m_templateUnit.getParentPath())
                            : ClassNames.BASE_TEMPLATE));
        m_writer.println("  implements "
                         + m_resolver.getFullyQualifiedIntfClassName(getPath())
                         + ".Intf");
        m_writer.openBlock();
        m_writer.println(m_templateUnit.getClassContent());
    }

    private void generateInitialize()
        throws IOException
    {
        m_writer.println("protected void initializeDefaultArguments()");
        m_writer.println("  throws Exception");
        m_writer.openBlock();
        if (m_templateUnit.hasParentPath())
        {
            m_writer.println("super.initializeDefaultArguments();");
        }
        for (Iterator i = m_templateUnit.getSignatureOptionalArgs();
             i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            if(arg.getDefault() != null)
            {
                m_writer.println(arg.getObfuscatedName()
                                 + " = " + arg.getDefault() + ";");
            }
        }
        m_writer.closeBlock();
        m_writer.println();
    }

    private void generateConstructor()
        throws IOException
    {
        m_writer.println("public " +  getClassName()
                       + "(" + ClassNames.TEMPLATE_MANAGER
                       + " p_templateManager, String p_path)");
        m_writer.openBlock();
        m_writer.println("super(p_templateManager, p_path);");
        m_writer.closeBlock();
        m_writer.println();
    }

    private void generatePrologue()
        throws IOException
    {
        String pkgName = m_resolver.getImplPackageName(getPath());
        if (pkgName.length() > 0)
        {
            m_writer.println("package " + pkgName + ";");
            m_writer.println();
        }
    }


    // FIXME - copied from IntfGenerator
    private void generateTemplateImplConstructor(String p_className)
    {
        m_writer.println("protected " + p_className
                         + "(" + ClassNames.TEMPLATE_MANAGER + " p_manager"
                         + ", String p_path)");
        m_writer.openBlock();
        m_writer.println("super(p_manager, p_path);");
        m_writer.closeBlock();
    }

    private void generateDefFargInterface(FragmentUnit p_fragmentUnit)
        throws IOException
    {
        m_writer.println("private static abstract class "
                         + p_fragmentUnit.getFragmentInterfaceName());
        m_writer.println("  extends " + ClassNames.BASE_TEMPLATE);
        m_writer.openBlock();
        generateTemplateImplConstructor(
            p_fragmentUnit.getFragmentInterfaceName());
        m_writer.println();
        m_writer.print  ("abstract public void render(");

        p_fragmentUnit.printRequiredArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.println("  throws java.io.IOException;");

        m_writer.print("abstract public " + ClassNames.RENDERER
                       + " makeRenderer(");
        p_fragmentUnit.printRequiredArgsDecl(m_writer);
        m_writer.println(");");
        m_writer.closeBlock();
        m_writer.println();
    }


    private void generateDefs()
        throws IOException
    {
        for (Iterator d = m_templateUnit.getDefUnits();
             d.hasNext(); )
        {
            DefUnit defUnit = (DefUnit) d.next();
            m_writer.println();
            for (Iterator f = defUnit.getFragmentArgs(); f.hasNext(); )
            {
                generateDefFargInterface(((FragmentArgument) f.next())
                                         .getFragmentUnit());
            }

            m_writer.print("private void __jamon_def__");
            m_writer.print(defUnit.getName());
            m_writer.print("(");
            defUnit.printAllArgsDecl(m_writer);
            m_writer.println(")");
            m_writer.print  ("  throws " + ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            defUnit.printArgDeobfuscations(m_writer);
            defUnit.printStatements(m_writer,
                                    m_resolver,
                                    m_describer);
            m_writer.closeBlock();
            m_writer.println();
        }
    }

    final static String CHILD_FARG_NAME = "_Jamon_Fragment__CHILD";

    private void generateRender()
        throws IOException
    {
        m_writer.print("public void render(");
        if(m_templateUnit.isParent())
        {
            m_writer.print("final " + ClassNames.CHILD_FARG + " "
                           + CHILD_FARG_NAME);
            if(m_templateUnit.hasSignatureRequiredArgs())
            {
                m_writer.print(", ");
            }
        }
        m_templateUnit.printRequiredArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        if(m_templateUnit.hasParentPath())
        {
            m_writer.println("super.render(");
            m_writer.indent(2);
            m_writer.print("new " + ClassNames.CHILD_FARG
                           + "(this.getTemplateManager()) ");
            m_writer.openBlock();
            m_writer.println("public void render() throws "
                             + ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            generateRenderBody();
            m_writer.closeBlock();
            if(m_templateUnit.hasRequiredParentArgs())
            {
                m_writer.closeBlock(",");
                m_templateUnit.printParentRequiredArgs(m_writer);
                m_writer.println(");");
            }
            else
            {
                m_writer.closeBlock(");");
            }
            m_writer.outdent(2);
        }
        else
        {
            generateRenderBody();
        }
        m_writer.closeBlock();
    }

    private void generateRenderBody()
        throws IOException
    {
        m_templateUnit.printArgDeobfuscations(m_writer);
        m_templateUnit.printStatements(m_writer,
                                       m_resolver,
                                       m_describer);
        m_writer.println("if (isAutoFlushEnabled())");
        m_writer.openBlock();
        m_writer.println("getWriter().flush();");
        m_writer.closeBlock();
    }

    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = m_templateUnit.getDeclaredOptionalArgs();
             i.hasNext(); )
        {
            m_writer.println();
            OptionalArgument arg = (OptionalArgument) i.next();
            String name = arg.getName();
            String type = arg.getType();
            m_writer.println
                ("public void set" + StringUtils.capitalize(name)
                 + "(" + type + " p_" + name + ")");
            m_writer.openBlock();
            m_writer.println(arg.getObfuscatedName()
                             + " = p_" + name + ";");
            m_writer.closeBlock();
            m_writer.println();
            m_writer.println("protected " + type + " "
                             + arg.getObfuscatedName() + ";");
        }
    }

    private void generateEpilogue()
        throws IOException
    {
        m_writer.println();
        m_writer.closeBlock();
    }

    private void generateImports()
        throws IOException
    {
        for (Iterator i = m_templateUnit.getImports();
             i.hasNext(); )
        {
            m_writer.println("import " + i.next() + ";");
        }
        m_writer.println();
    }
}
