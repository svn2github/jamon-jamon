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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

public class ProxyGenerator
{
    public ProxyGenerator(Writer p_writer,
                          TemplateDescriber p_describer,
                          TemplateUnit p_templateUnit)
    {
        m_writer = new IndentingWriter(p_writer);
        m_describer = p_describer;
        m_templateUnit = p_templateUnit;
    }

    public void generateClassSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateConstructors();
        generateSignature();
        generateArgArrays(m_templateUnit, "");
        generateMethodArrays();
        generateInheritanceDepth();
        generateIntf();
        generateImplData();
        generateOptionalArgs();
        generateFragmentInterfaces(false);
        if (! m_templateUnit.isParent())
        {
            generateConstructImpl();
            generateMakeRenderer(! m_templateUnit.isParent());
            generateRender();
            generateSetWriter(getClassName());
            generateEscaping(getClassName());
            generateSetAutoFlush(getClassName());
        }
        if (m_templateUnit.isParent())
        {
            generateParentRendererClass();
        }
        if (m_templateUnit.hasParentPath() && ! m_templateUnit.isParent())
        {
            generateMakeParentRenderer();
        }
        generateEpilogue();
        m_writer.finish();
    }

    private final TemplateDescriber m_describer;
    private IndentingWriter m_writer;
    private final TemplateUnit m_templateUnit;

    private void generateImports()
    {
        for (Iterator i = m_templateUnit.getImports();
             i.hasNext(); )
        {
            m_writer.println("import " + i.next() + ";");
        }
        m_writer.println();
    }

    private String getClassName()
    {
        return PathUtils.getIntfClassName(m_templateUnit.getName());
    }

    private String getPackageName()
    {
        return PathUtils.getIntfPackageName(m_templateUnit.getName());
    }

    private void generatePrologue()
    {
        String pkgName = getPackageName();
        if (pkgName.length() > 0)
        {
            m_writer.println("package " + pkgName + ";");
            m_writer.println();
        }
    }


    private void generateConstructors()
    {
        m_writer.println();
        m_writer.println
            ("public " + getClassName()
             + "(" + ClassNames.TEMPLATE_MANAGER + " p_manager)");
        m_writer.openBlock();
        m_writer.println(" super(p_manager);");
        m_writer.closeBlock();

        m_writer.println();
        if (m_templateUnit.isParent())
        {
            m_writer.println("protected "
                             + getClassName()
                             + "(String p_path)");
            m_writer.openBlock();
            m_writer.println("super(p_path);");
            m_writer.closeBlock();
            m_writer.println();
        }
        else
        {
            m_writer.println("public " + getClassName() + "()");
            m_writer.openBlock();
            m_writer.println(" super(\"" + m_templateUnit.getName() + "\");");
            m_writer.closeBlock();
            m_writer.println();
        }
    }

    private void generateFragmentInterfaces(boolean p_inner)
    {
        for (Iterator f = m_templateUnit.getDeclaredFragmentArgs();
             f.hasNext(); )
        {
            ((FragmentArgument) f.next()).getFragmentUnit()
                .printInterface(m_writer, "public", ! p_inner);
            m_writer.println();
        }
        m_writer.println();
    }

    private void generateDeclaration()
    {
        m_writer.print("public ");
        if(m_templateUnit.isParent())
        {
            m_writer.print("abstract ");
        }
        m_writer.println("class " + getClassName());
        m_writer.println("  extends "
                         + (m_templateUnit.hasParentPath()
                            ? PathUtils.getFullyQualifiedIntfClassName(
                                m_templateUnit.getParentPath())
                            : ClassNames.TEMPLATE));
        m_templateUnit.printInterfaces(m_writer);
        m_writer.openBlock();
    }

    private void generateArgArrays(Unit p_unit, String p_prefix)
    {
        List parentRequiredArgs = new LinkedList();
        printArgNames(p_prefix + "REQUIRED",
                      p_unit.getSignatureRequiredArgs());
        printArgTypes(p_prefix + "REQUIRED",
                      p_unit.getSignatureRequiredArgs());
        printArgNames(p_prefix + "OPTIONAL",
                      p_unit.getSignatureOptionalArgs());
        printArgTypes(p_prefix + "OPTIONAL",
                      p_unit.getSignatureOptionalArgs());

        m_writer.print("public static final String[] "
                       + p_prefix + "FRAGMENT_ARG_NAMES = ");
        m_writer.openBlock();
        for (Iterator i = p_unit.getFragmentArgs(); i.hasNext(); )
        {
            printQuoted(((FragmentArgument) i.next()).getName());
        }
        m_writer.closeBlock(";");

        for (Iterator i = p_unit.getFragmentArgs(); i.hasNext(); )
        {
            FragmentArgument frag = (FragmentArgument) i.next();
            printArgNames(p_prefix + "FRAGMENT_ARG_" + frag.getName(),
                          frag.getFragmentUnit().getRequiredArgs());
        }
    }

    private void generateMethodArrays()
    {
        m_writer.print("public static final String[] METHOD_NAMES = ");
        m_writer.openBlock();
        for (Iterator i = m_templateUnit.getSignatureMethodUnits();
             i.hasNext(); )
        {
            printQuoted(((MethodUnit) i.next()).getName());
        }
        m_writer.closeBlock(";");
        for (Iterator i = m_templateUnit.getSignatureMethodUnits();
             i.hasNext(); )
        {
            MethodUnit methodUnit = (MethodUnit) i.next();
            generateArgArrays(methodUnit,
                              "METHOD_" + methodUnit.getName() + "_");
        }
    }

    private void printArgNames(String p_argsType, Iterator p_args)
    {
        m_writer.print("public static final String[] "
                       + p_argsType + "_ARG_NAMES = ");
        m_writer.openBlock();
        while (p_args.hasNext())
        {
            printQuoted(((AbstractArgument) p_args.next()).getName());
        }
        m_writer.closeBlock(";");
    }

    private void printArgTypes(String p_argsType, Iterator p_args)
    {
        m_writer.print("public static final String[] "
                       + p_argsType + "_ARG_TYPES = ");
        m_writer.openBlock();
        while (p_args.hasNext())
        {
            printQuoted(((AbstractArgument) p_args.next()).getType());
        }
        m_writer.closeBlock(";");
    }

    private void printQuoted(String p_string)
    {
        m_writer.print("\"" + p_string + "\", ");
    }

    private void generateConstructImpl()
    {
        m_writer.println();
        m_writer.print(
            "protected " + ClassNames.BASE_TEMPLATE + " constructImpl"
            + "(Class p_class, "
            + ClassNames.TEMPLATE_MANAGER + " p_manager)");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.println("try");
        m_writer.openBlock();
        m_writer.println("return (" + ClassNames.BASE_TEMPLATE + ") p_class");
        m_writer.indent(2);
        m_writer.println(".getConstructor(new Class [] { "
                         + ClassNames.TEMPLATE_MANAGER + ".class"
                         + ", ImplData.class })");
        m_writer.println(".newInstance(new Object [] "
                         + "{ p_manager, getImplData() });");
        m_writer.outdent(2);
        m_writer.closeBlock();
        m_writer.println("catch (RuntimeException e)");
        m_writer.openBlock();
        m_writer.println("throw e;");
        m_writer.closeBlock();
        m_writer.println("catch (Exception e)");
        m_writer.openBlock();
        m_writer.println("throw new " + ClassNames.JAMON_EXCEPTION + "(e);");
        m_writer.closeBlock();
        m_writer.closeBlock();

        m_writer.println();
        m_writer.print(
            "protected " + ClassNames.BASE_TEMPLATE + " constructImpl("
            + ClassNames.TEMPLATE_MANAGER + " p_manager)");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.println(
            "return new "
            + PathUtils.getImplClassName(m_templateUnit.getName())
            + "(p_manager, (ImplData) getImplData());");
        m_writer.closeBlock();
    }

    private void generateRender()
    {
        m_writer.print((m_templateUnit.isParent() ? "protected" : "public")
                       + " void render(");
        m_templateUnit.printRenderArgsDecl(m_writer);
        m_writer.println(")");

        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.println("ImplData implData = (ImplData) getImplData();");
        for (Iterator i = m_templateUnit.getRenderArgs(); i.hasNext(); )
        {
            AbstractArgument arg = (AbstractArgument) i.next();
            m_writer.println("implData." + arg.getSetterName()
                             + "(" + arg.getName() + ");");
        }

        m_writer.println(
            "Intf instance = (Intf) getTemplateManager().constructImpl(this);"
            );

        m_writer.println("instance.escapeWith(getEscaping());");
        m_writer.println("instance.render();");
        m_writer.println("reset();");
        m_writer.closeBlock();
        m_writer.println();
    }

    private void generateMakeRenderer(boolean p_public)
    {
        m_writer.print( p_public
                        ? "public "
                        : "protected " );
        m_writer.print(ClassNames.RENDERER + " makeRenderer(");
        m_templateUnit.printRenderArgsDecl(m_writer);
        m_writer.println(")");

        m_writer.openBlock();
        m_writer.print(  "return new " + ClassNames.RENDERER + "() ");
        m_writer.openBlock();
        m_writer.println("public void renderTo("
                         + ClassNames.WRITER + " p_writer)");
        m_writer.println(  "  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.println("writeTo(p_writer);");
        m_writer.print  ("render(");
        m_templateUnit.printRenderArgs(m_writer);
        m_writer.println(");");
        m_writer.closeBlock();
        m_writer.closeBlock(";");
        m_writer.closeBlock();
        m_writer.println();
    }

    private void generateImplData()
    {
        m_writer.println("protected static class ImplData");
        m_writer.println("  extends ");
        if(m_templateUnit.hasParentPath())
        {
            m_writer.println(PathUtils.getFullyQualifiedIntfClassName(
                m_templateUnit.getParentPath())
                             + ".ImplData");
        }
        else
        {
            m_writer.println(ClassNames.IMPL_DATA);
        }
        m_writer.println();
        m_writer.openBlock();
        for (Iterator i = m_templateUnit.getDeclaredArgs(); i.hasNext(); )
        {
            ((AbstractArgument) i.next()).generateImplDataCode(m_writer);
        }
        m_writer.closeBlock();


        if (! m_templateUnit.isParent())
        {
            m_writer.println("protected " + ClassNames.IMPL_DATA
                             + " makeImplData()");
            m_writer.openBlock();
            m_writer.println("return new ImplData();");
            m_writer.closeBlock();
        }
    }

    private void generateOptionalArgs()
    {
        for (Iterator i = m_templateUnit.getDeclaredOptionalArgs();
             i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            m_writer.println();
            m_writer.println("protected " + arg.getType() + " "
                             + arg.getName() + ";");
            m_writer.print("public final ");
            String pkgName = getPackageName();
            if (pkgName.length() > 0)
            {
                m_writer.print(pkgName + ".");
            }
            m_writer.print(getClassName());
            m_writer.println(
                " " + arg.getSetterName()
                + "(" + arg.getType() +" p_" + arg.getName() + ")");
            m_writer.openBlock();
            m_writer.println(
                "((ImplData) getImplData())."
                + arg.getSetterName() + "(p_" + arg.getName() + ");");
            m_writer.println("return this;");
            m_writer.closeBlock();
        }
    }

    private void generateSignature()
        throws IOException
    {
        m_writer.print("public static final String SIGNATURE = \"");
        m_writer.print(m_templateUnit.getSignature());
        m_writer.println("\";");
    }

    private void generateInheritanceDepth()
    {
        m_writer.print("public static final int INHERITANCE_DEPTH = ");
        m_writer.print(String.valueOf(m_templateUnit.getInheritanceDepth()));
        m_writer.println(";");
    }

    private void generateIntf()
    {
        m_writer.println("protected interface Intf");
        m_writer.print("  extends "
                       + (m_templateUnit.hasParentPath()
                          ? PathUtils.getFullyQualifiedIntfClassName(
                              m_templateUnit.getParentPath()) + ".Intf"
                          : ClassNames.TEMPLATE_INTF));
        m_writer.openBlock();

        generateFragmentInterfaces(true);

        if(! m_templateUnit.isParent())
        {
            m_writer.println(
                "void render() throws " + ClassNames.IOEXCEPTION + ";");
            m_writer.println();
        }
        m_writer.closeBlock();
    }

    private void generateParentRendererClass()
    {
        m_writer.println("public abstract class ParentRenderer");
        m_writer.openBlock();
        m_writer.println("protected ParentRenderer() {}");

        for (Iterator i = m_templateUnit.getSignatureOptionalArgs();
             i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            m_writer.println();
            String name = arg.getName();
            m_writer.print("public final ParentRenderer ");
            m_writer.println(arg.getSetterName()
                             + "(" + arg.getType() +" p_" + name + ")");
            m_writer.openBlock();
            m_writer.println(getClassName() + ".this." + arg.getSetterName()
                             + "(" + "p_" + name + ");");
            m_writer.println("return this;");
            m_writer.closeBlock();
        }

        if (! m_templateUnit.hasParentPath())
        {
            m_writer.print("public void render(");
            m_templateUnit.printDeclaredRenderArgsDecl(m_writer);
            m_writer.println(")");
            m_writer.print("  throws " + ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            m_writer.print("renderChild(");
            m_templateUnit.printDeclaredRenderArgs(m_writer);
            m_writer.println(");");
            m_writer.closeBlock();

            generateSetWriter("ParentRenderer");
            generateEscaping("ParentRenderer");
            generateSetAutoFlush("ParentRenderer");

            generateMakeRenderer(true);
        }
        else
        {
            generateMakeParentRenderer();
        }

        m_writer.print("protected abstract void renderChild(");
        m_templateUnit.printRenderArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION + ";");

        m_writer.closeBlock();
    }

    private void generateMakeParentRenderer()
    {
        String parentRendererClass =
            PathUtils.getFullyQualifiedIntfClassName(
                m_templateUnit.getParentPath()) + ".ParentRenderer";
        m_writer.print("public " + parentRendererClass
                       + " makeParentRenderer(");
        m_templateUnit.printDeclaredRenderArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.openBlock();
        m_writer.print("return new " + parentRendererClass + "() ");
        m_writer.openBlock();
        m_writer.print("protected void renderChild(");
        m_templateUnit.printParentRenderArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        if (m_templateUnit.isParent())
        {
            m_writer.print
                (PathUtils.getFullyQualifiedIntfClassName(getClassName())
                 + ".ParentRenderer.this.renderChild(");
            m_templateUnit.printRenderArgs(m_writer);
            m_writer.println(");");
        }
        else
        {
            m_writer.print(
                PathUtils.getFullyQualifiedIntfClassName(getClassName())
                + ".this.render(");
            m_templateUnit.printRenderArgs(m_writer);
            m_writer.println(");");
        }
        m_writer.closeBlock();
        m_writer.closeBlock(";");
        m_writer.closeBlock();
    }

    private void generateSetWriter(String p_returnClassName)
    {
        m_writer.println();
        m_writer.println("public " + p_returnClassName
                         + " writeTo(" + ClassNames.WRITER + " p_writer)");
        m_writer.openBlock();
        m_writer.println("getImplData().setWriter(p_writer);");
        m_writer.println("return this;");
        m_writer.closeBlock();
    }

    private void generateSetAutoFlush(String p_returnClassName)
    {
        m_writer.println();
        m_writer.println("public " + p_returnClassName
                         + " autoFlush(boolean p_autoFlush)");
        m_writer.openBlock();
        m_writer.println("getImplData().setAutoFlush(p_autoFlush);");
        m_writer.println("return this;");
        m_writer.closeBlock();
    }

    private void generateEscaping(String p_returnClassName)
    {
        m_writer.println();
        m_writer.println("/** @deprecated use #escapeWith */");
        m_writer.println("public " + p_returnClassName + " escaping("
                         + ClassNames.ESCAPING + " p_escaping)");
        m_writer.openBlock();
        m_writer.println("return escapeWith(p_escaping);");
        m_writer.closeBlock();
        m_writer.println();
        m_writer.println("public " + p_returnClassName + " escapeWith("
                         + ClassNames.ESCAPING + " p_escaping)");
        m_writer.openBlock();
        m_writer.println("escape(p_escaping);");
        m_writer.println("return this;");
        m_writer.closeBlock();
    }

    private void generateEpilogue()
    {
        m_writer.println();
        m_writer.closeBlock();
    }
}
