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

public class IntfGenerator
{
    public IntfGenerator(Writer p_writer,
                         TemplateResolver p_resolver,
                         TemplateDescriber p_describer,
                         TemplateUnit p_templateUnit)
    {
        m_writer = new IndentingWriter(p_writer);
        m_resolver = p_resolver;
        m_describer = p_describer;
        m_templateUnit = p_templateUnit;
    }

    public void generateClassSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateConstructor();
        generateSignature();
        generateArgArrays(m_templateUnit, "");
        generateMethodArrays();
        generateIntf();
        generateGetInstance();
        generateGetPath();
        generateOptionalArgs();
        generateFragmentInterfaces(false);
        if (! m_templateUnit.isParent())
        {
            generateMakeRenderer();
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

    private final TemplateResolver m_resolver;
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
        return m_resolver.getIntfClassName(m_templateUnit.getName());
    }

    private String getPackageName()
    {
        return m_resolver.getIntfPackageName(m_templateUnit.getName());
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


    private void generateConstructor()
    {
        m_writer.println();
        m_writer.println
            ("public " + getClassName()
             + "(" + ClassNames.TEMPLATE_MANAGER + " p_manager)");
        m_writer.openBlock();
        m_writer.println(" super(p_manager);");
        m_writer.closeBlock();
    }

    private void generateFragmentInterface(FragmentUnit p_fragmentUnit,
                                           boolean p_inner)
    {
        String className = p_fragmentUnit.getFragmentInterfaceName();
        m_writer.println("public static abstract class " + className);
        if (!p_inner)
        {
            m_writer.println("  extends " + getClassName() + ".Intf."
                             + className);
            m_writer.openBlock();
            generateTemplateImplConstructor(className);
            m_writer.closeBlock();
        }
        else
        {
            m_writer.println(" extends " + ClassNames.BASE_TEMPLATE);
            m_writer.openBlock();
            generateTemplateImplConstructor(className);
            m_writer.println();
            m_writer.print  ("abstract public void render(");
            p_fragmentUnit.printRequiredArgsDecl(m_writer);
            m_writer.println(")");
            m_writer.println("  throws " + ClassNames.IOEXCEPTION + ";");
            m_writer.print("abstract public " + ClassNames.RENDERER
                           + " makeRenderer(");
            p_fragmentUnit.printRequiredArgsDecl(m_writer);
            m_writer.println(");");
            m_writer.closeBlock();
        }
        m_writer.println();
    }

    private void generateTemplateImplConstructor(String p_className)
    {
        m_writer.println("protected " + p_className
                         + "(" + ClassNames.TEMPLATE_MANAGER + " p_manager"
                         + ", String p_path)");
        m_writer.openBlock();
        m_writer.println("super(p_manager, p_path);");
        m_writer.closeBlock();
    }

    private void generateFragmentInterfaces(boolean p_inner)
    {
        for (Iterator f = m_templateUnit.getDeclaredFragmentArgs();
             f.hasNext(); )
        {
            generateFragmentInterface(((FragmentArgument) f.next())
                                          .getFragmentUnit(),
                                      p_inner);
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
                            ? m_resolver.getFullyQualifiedIntfClassName(
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

    private void generateRender()
    {
        m_writer.print( m_templateUnit.isParent()
                        ? "protected void render("
                        : "public void render(" );
        m_templateUnit.printRequiredArgsDecl(m_writer);
        m_writer.println(")");

        m_writer.println("  throws java.io.IOException");
        m_writer.openBlock();
        m_writer.println("try");
        m_writer.openBlock();
        m_writer.print  ("getInstance().render(");
        m_templateUnit.printRequiredArgs(m_writer);
        m_writer.println(");");
        m_writer.closeBlock();
        m_writer.println("finally");
        m_writer.openBlock();
        m_writer.println("releaseInstance();");
        m_writer.closeBlock();
        m_writer.closeBlock();
        m_writer.println();
    }


    private void generateMakeRenderer()
    {
        m_writer.print( m_templateUnit.isParent()
                        ? "protected "
                        : "public " );
        m_writer.print( ClassNames.RENDERER + " makeRenderer(");
        m_templateUnit.printRequiredArgsDecl(m_writer);
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
        m_templateUnit.printRequiredArgs(m_writer);
        m_writer.println(");");
        m_writer.closeBlock();
        m_writer.closeBlock(";");
        m_writer.closeBlock();
        m_writer.println();
    }



    private void generateOptionalArgs()
    {
        for (Iterator i = m_templateUnit.getDeclaredOptionalArgs();
             i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            m_writer.println();
            String name = arg.getName();
            m_writer.print("public final ");
            String pkgName = getPackageName();
            if (pkgName.length() > 0)
            {
                m_writer.print(pkgName + ".");
            }
            m_writer.print(getClassName());
            m_writer.println(" " + arg.getSetterName()
                             + "(" + arg.getType() +" p_" + name + ")");
            m_writer.print  ("  throws ");
            m_writer.println(ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            m_writer.println("getInstance()." + arg.getSetterName()
                             + "(" + "p_" + name + ");");
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

    private void generateIntf()
    {
        m_writer.println("protected interface Intf");
        m_writer.print("  extends "
                       + (m_templateUnit.hasParentPath()
                          ? m_resolver.getFullyQualifiedIntfClassName(
                              m_templateUnit.getParentPath()) + ".Intf"
                          : ClassNames.TEMPLATE_INTF));
        m_writer.openBlock();

        generateFragmentInterfaces(true);

        if(! m_templateUnit.isParent())
        {
            m_writer.print("void render(");
            m_templateUnit.printRequiredArgsDecl(m_writer);
            m_writer.println(")");
            m_writer.println("  throws java.io.IOException;");
            m_writer.println();
        }

        for (Iterator i = m_templateUnit.getDeclaredOptionalArgs();
             i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            m_writer.println();
            m_writer.println
                ("void " + arg.getSetterName()
                 + "(" + arg.getType() + " " + arg.getName() + ");");
        }
        m_writer.closeBlock();
    }

    private void generateParentRendererClass()
    {
        m_writer.println("public abstract class ParentRenderer");
        m_writer.openBlock();
        m_writer.println("protected ParentRenderer() {}");

        for (Iterator i = m_templateUnit.getDeclaredOptionalArgs();
             i.hasNext(); )
        {
            OptionalArgument arg = (OptionalArgument) i.next();
            m_writer.println();
            String name = arg.getName();
            m_writer.print("public final ParentRenderer ");
            m_writer.println(arg.getSetterName()
                             + "(" + arg.getType() +" p_" + name + ")");
            m_writer.print  ("  throws ");
            m_writer.println(ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            m_writer.println(getClassName() + ".this." + arg.getSetterName()
                             + "(" + "p_" + name + ");");
            m_writer.println("return this;");
            m_writer.closeBlock();
        }

        if (! m_templateUnit.hasParentPath())
        {
            m_writer.print("public void render(");
            m_templateUnit.printDeclaredRequiredArgsDecl(m_writer);
            m_writer.println(")");
            m_writer.print("  throws " + ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            m_writer.print("renderChild(");
            m_templateUnit.printDeclaredRequiredArgs(m_writer);
            m_writer.println(");");
            m_writer.closeBlock();

            generateSetWriter("ParentRenderer");
            generateEscaping("ParentRenderer");
            generateSetAutoFlush("ParentRenderer");
        }
        else
        {
            generateMakeParentRenderer();
        }

        m_writer.print("protected abstract void renderChild(");
        m_templateUnit.printRequiredArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION + ";");

        m_writer.closeBlock();
    }

    private void generateMakeParentRenderer()
    {
        String parentRendererClass =
            m_resolver.getFullyQualifiedIntfClassName(
                m_templateUnit.getParentPath()) + ".ParentRenderer";
        m_writer.print("public " + parentRendererClass
                       + " makeParentRenderer(");
        m_templateUnit.printDeclaredRequiredArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.print("return new " + parentRendererClass + "() ");
        m_writer.openBlock();
        m_writer.print("protected void renderChild(");
        m_templateUnit.printParentRequiredArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        if (m_templateUnit.isParent())
        {
            m_writer.print
                (m_resolver.getFullyQualifiedIntfClassName(getClassName())
                 + ".ParentRenderer.this.renderChild(");
            m_templateUnit.printRequiredArgs(m_writer);
            m_writer.println(");");
        }
        else
        {
            m_writer.print(
                m_resolver.getFullyQualifiedIntfClassName(getClassName())
                + ".this.render(");
            m_templateUnit.printRequiredArgs(m_writer);
            m_writer.println(");");
        }
        m_writer.closeBlock();
        m_writer.closeBlock(";");
        m_writer.closeBlock();
    }

    private void generateGetInstance()
    {
        m_writer.println();
        m_writer.println("private Intf getInstance()");
        m_writer.print("  throws " + ClassNames.IOEXCEPTION);
        m_writer.println();
        m_writer.openBlock();
        m_writer.println("return (Intf) getUntypedInstance();");
        m_writer.closeBlock();
    }

    private void generateGetPath()
    {
        if (! m_templateUnit.isParent())
        {
            m_writer.println();
            m_writer.println("protected String getPath()");
            m_writer.openBlock();
            m_writer.println("return \"" + m_templateUnit.getName() + "\";");
            m_writer.closeBlock();
        }
    }

    private void generateSetWriter(String p_returnClassName)
    {
        m_writer.println();
        m_writer.println("public " + p_returnClassName
                         + " writeTo(" + ClassNames.WRITER + " p_writer)");
        m_writer.print  ("  throws ");
        m_writer.println(ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.println("getInstance().writeTo(p_writer);");
        m_writer.println("return this;");
        m_writer.closeBlock();
    }

    private void generateSetAutoFlush(String p_returnClassName)
    {
        m_writer.println();
        m_writer.println("public " + p_returnClassName
                         + " autoFlush(boolean p_autoFlush)");
        m_writer.print  ("  throws ");
        m_writer.println(ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.println("getInstance().autoFlush(p_autoFlush);");
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
