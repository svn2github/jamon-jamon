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
        generateSetOptionalArguments();
        generateConstructor();
        generateRender();
        generateDefs();
        generateMethods();
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
        m_writer.println("  implements " + getProxyClassName() + ".Intf");
        m_writer.openBlock();
        for (Iterator i = m_templateUnit.getVisibleArgs(); i.hasNext(); )
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            m_writer.println(
                "private final " + arg.getType() + " " + arg.getName() + ";");
        }
        m_writer.println(m_templateUnit.getClassContent());
    }

    private void generateSetOptionalArguments()
    {
        m_writer.println("protected static " + getImplDataClassName()
                         + " " + SET_OPTS + "("
                         + getImplDataClassName() + " p_implData)");
        m_writer.openBlock();
        for (Iterator i = m_templateUnit.getSignatureOptionalArgs();
             i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            if(arg.getDefault() != null)
            {
                m_writer.println(
                    "if(! p_implData." + arg.getIsNotDefaultName() + "())");
                m_writer.openBlock();
                m_writer.println("p_implData." + arg.getSetterName() + "("
                                 + arg.getDefault() + ");");
                m_writer.closeBlock();
            }
        }
        if (m_templateUnit.hasParentPath())
        {
            m_writer.println(getParentImplClassName() + "."
                             + SET_OPTS + "(p_implData);");
        }
        m_writer.println("return p_implData;");
        m_writer.closeBlock();
    }

    private void generateConstructor()
        throws IOException
    {
        m_writer.println("public " +  getClassName()
                         + "(" + ClassNames.TEMPLATE_MANAGER
                         + " p_templateManager, String p_path, "
                         + getImplDataClassName() + " p_implData)");
        m_writer.openBlock();
        m_writer.println(
            "super(p_templateManager, p_path, " + SET_OPTS + "(p_implData));");
        for (Iterator i = m_templateUnit.getVisibleArgs(); i.hasNext(); )
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            m_writer.println(arg.getName()
                             + " = p_implData." + arg.getGetterName() + "();");
        }
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

    private void generateInnerUnitFargInterface(FragmentUnit p_fragmentUnit,
                                                boolean p_private)
        throws IOException
    {
        m_writer.println((p_private ? "private" : "protected")
                         + " static abstract class "
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
        for (Iterator i = m_templateUnit.getDefUnits(); i.hasNext(); )
        {
            DefUnit defUnit = (DefUnit) i.next();
            m_writer.println();
            for (Iterator f = defUnit.getFragmentArgs(); f.hasNext(); )
            {
                generateInnerUnitFargInterface(((FragmentArgument) f.next())
                                               .getFragmentUnit(),
                                               true);
            }

            m_writer.print("private void __jamon_innerUnit__");
            m_writer.print(defUnit.getName());
            m_writer.print("(");
            defUnit.printAllArgsDecl(m_writer);
            m_writer.println(")");
            m_writer.println("  throws " + ClassNames.IOEXCEPTION);
            defUnit.generateRenderBody(m_writer, m_resolver, m_describer);
            m_writer.println();
        }
    }

    private void generateMethods()
        throws IOException
    {
        for (Iterator i = m_templateUnit.getDeclaredMethodUnits();
             i.hasNext(); )
        {
            generateMethodIntf((MethodUnit) i.next());
        }
        for (Iterator i = m_templateUnit.getImplementedMethodUnits();
             i.hasNext(); )
        {
            generateMethodImpl((MethodUnit) i.next());
        }
    }

    private void generateMethodIntf(MethodUnit p_methodUnit)
        throws IOException
    {
        m_writer.println();
        for (Iterator f = p_methodUnit.getFragmentArgs(); f.hasNext(); )
        {
            generateInnerUnitFargInterface(((FragmentArgument) f.next())
                                           .getFragmentUnit(),
                                           false);
        }

    }


    private void generateMethodImpl(MethodUnit p_methodUnit)
        throws IOException
    {
        //FIXME - cut'n'pasted from generateDefs
        m_writer.println();
        m_writer.print("protected void __jamon_innerUnit__");
        m_writer.print(p_methodUnit.getName());
        m_writer.print("(");
        p_methodUnit.printAllArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        p_methodUnit.generateRenderBody(m_writer, m_resolver, m_describer);
        m_writer.println();

        //FIXME - only generate these for optional args we provide new
        //defaults for.
        for (Iterator i = p_methodUnit.getSignatureOptionalArgs();
             i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            m_writer.println("protected " + arg.getType() + " "
                             + p_methodUnit.getOptionalArgDefaultMethod(arg)
                             + "()");
            m_writer.println("  throws " + ClassNames.JAMON_EXCEPTION);
            m_writer.openBlock();
            m_writer.println("return " + arg.getDefault() + ";");
            m_writer.closeBlock();
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
        }
        m_writer.println(")");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        if(m_templateUnit.hasParentPath())
        {
            m_writer.openBlock();
            m_writer.println("super.render(");
            m_writer.indent(2);
            m_writer.print("new " + ClassNames.CHILD_FARG
                           + "(this.getTemplateManager()) ");
            m_writer.openBlock();
            m_writer.println("public void render() throws "
                             + ClassNames.IOEXCEPTION);
            m_templateUnit.generateRenderBody(m_writer,
                                              m_resolver,
                                              m_describer);
            m_writer.closeBlock(");");
            m_writer.outdent(2);
            m_writer.closeBlock();
        }
        else
        {
            m_templateUnit.generateRenderBody(m_writer,
                                              m_resolver,
                                              m_describer);
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

    private String getProxyClassName()
    {
        return m_resolver.getFullyQualifiedIntfClassName(getPath());
    }

    private String getImplDataClassName()
    {
        return getProxyClassName() + ".ImplData";
    }

    private String getParentImplClassName()
    {
        return m_resolver.getFullyQualifiedImplClassName(
            m_templateUnit.getParentPath());
    }


    private final static String SET_OPTS = "__jamon_setOptionalArguments";
}
