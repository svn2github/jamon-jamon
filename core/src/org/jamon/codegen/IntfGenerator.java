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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;

import org.jamon.util.StringUtils;

public class IntfGenerator
{
    public IntfGenerator(TemplateResolver p_resolver,
                         String p_templatePath,
                         BaseAnalyzer p_analyzer,
                         Writer p_writer)
        throws IOException
    {
        m_writer = new IndentingWriter(p_writer);
        m_resolver = p_resolver;
        m_path = p_templatePath;
        m_analyzer = p_analyzer;
    }

    public void generateClassSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateConstructor();
        generateIntf();
        generateSignature();
        generateFargInterfaces(false);
        generateMakeRenderer();
        generateRender();
        generateOptionalArgs();
        generateFargInfo();
        generateGetInstance();
        generateSetWriter();
        generateEscaping();
        generateEpilogue();
    }

    private final TemplateResolver m_resolver;
    private IndentingWriter m_writer;
    private final BaseAnalyzer m_analyzer;
    private final String m_path;

    private String getPath()
    {
        return m_path;
    }

    private void generateImports()
        throws IOException
    {
        for (Iterator i = m_analyzer.getImports(); i.hasNext(); /* */ )
        {
            m_writer.println("import " + i.next() + ";");
        }
        m_writer.println();
    }

    private String getClassName()
    {
        return m_resolver.getIntfClassName(getPath());
    }

    private String getPackageName()
    {
        return m_resolver.getIntfPackageName(getPath());
    }

    private void generatePrologue()
        throws IOException
    {
        String pkgName = getPackageName();
        if (pkgName.length() > 0)
        {
            m_writer.println("package " + pkgName + ";");
            m_writer.println();
        }
    }


    private void generateConstructor()
        throws IOException
    {
        m_writer.println();
        m_writer.println("public " + getClassName()
                         + "(" + ClassNames.TEMPLATE_MANAGER + " p_manager)");
        m_writer.openBlock();
        m_writer.println(" super(p_manager);");
        m_writer.closeBlock();
    }


    private void generateFargInterface(FargInfo p_fargInfo, boolean p_inner)
        throws IOException
    {
        m_writer.println("  public static interface Fragment_"
                         + p_fargInfo.getName());
        if (!p_inner)
        {
            m_writer.println("  extends " + getClassName() + ".Intf.Fragment_"
                           + p_fargInfo.getName());
            m_writer.println("{ }");
        }
        else
        {
            m_writer.println(" extends " + ClassNames.TEMPLATE_INTF);
            m_writer.openBlock();
            m_writer.print  ("void render(");
            for (Iterator a = p_fargInfo.getArgumentNames(); a.hasNext(); /* */)
            {
                String argName = (String) a.next();
                m_writer.print(p_fargInfo.getArgumentType(argName));
                m_writer.print(" ");
                m_writer.print(argName);
                if (a.hasNext())
                {
                    m_writer.print(", ");
                }
            }
            m_writer.println(")");
            m_writer.println("  throws " + ClassNames.IOEXCEPTION + ";");
            m_writer.print(ClassNames.RENDERER + " makeRenderer(");
            for (Iterator a = p_fargInfo.getArgumentNames(); a.hasNext(); /* */)
            {
                String argName = (String) a.next();
                m_writer.print(p_fargInfo.getArgumentType(argName));
                m_writer.print(" ");
                m_writer.print(argName);
                if (a.hasNext())
                {
                    m_writer.print(", ");
                }
            }
            m_writer.println(");");
            m_writer.closeBlock();
        }
        m_writer.println();
    }

    private void generateFargInterfaces(boolean p_inner)
        throws IOException
    {
        for (Iterator f = m_analyzer.getUnitInfo().getFargNames();
             f.hasNext();
             /* */)
        {
            generateFargInterface(m_analyzer.getFargInfo((String)f.next()),
                                  p_inner);
        }
        m_writer.println();
    }

    private void generateFargInfo()
        throws IOException
    {
        m_writer.print("public static final String[] FARGNAMES = ");
        m_writer.openBlock();
        for (Iterator f = m_analyzer.getUnitInfo().getFargNames();
             f.hasNext();
             /* */)
        {
            m_writer.print("\"" + (String)f.next() + "\"");
            if (f.hasNext())
            {
                m_writer.print(",");
            }
            m_writer.println();
        }
        m_writer.closeBlock(";");
        m_writer.println();
        for (Iterator f = m_analyzer.getUnitInfo().getFargNames();
             f.hasNext();
             /* */)
        {
            String name = (String)f.next();
            m_writer.print("public static final java.util.Map FARGINFO_");
            m_writer.print(name);
            m_writer.println(" = new java.util.HashMap();");
            m_writer.print("public static class init_" + name);
            m_writer.openBlock();
            m_writer.print("    static");
            m_writer.openBlock();
            FargInfo info = m_analyzer.getFargInfo(name);
            for (Iterator a = info.getArgumentNames(); a.hasNext(); /* */)
            {
                m_writer.print("FARGINFO_");
                m_writer.print(name);
                String an = (String) a.next();
                m_writer.print(".put(\"");
                m_writer.print(an);
                m_writer.print("\",\"");
                m_writer.print(info.getArgumentType(an));
                m_writer.println("\");");
            }
            m_writer.closeBlock();
            m_writer.closeBlock();
            m_writer.println("public static final init_" + name
                             + " init2_" + name
                             + " = new init_" + name + "();");
        }
        m_writer.println();
    }


    private void generateDeclaration()
        throws IOException
    {
        m_writer.println("public class " + getClassName());
        m_writer.println("  extends " + ClassNames.TEMPLATE);
        m_writer.openBlock();
    }

    private void generateRender()
        throws IOException
    {
        m_writer.print("public void render(");
        m_analyzer.getUnitInfo().printRequiredArgsDecl(m_writer);
        m_writer.println(")");

        m_writer.println("  throws java.io.IOException");
        m_writer.openBlock();
        m_writer.println("try");
        m_writer.openBlock();
        m_writer.print  ("getInstance().render(");
        m_analyzer.getUnitInfo().printRequiredArgs(m_writer);
        m_writer.println(");");
        m_writer.closeBlock();
        m_writer.println("finally");
        m_writer.openBlock();
        m_writer.println("releaseInstance();");
        m_writer.closeBlock();
        m_writer.closeBlock();


        m_writer.println();
        m_writer.print("public static final String[] REQUIRED_ARGS =");
        m_writer.openBlock();

        for (Iterator i = m_analyzer.getUnitInfo().getRequiredArgs();
             i.hasNext();
             /* */)
        {
            m_writer.print("\"" + ((Argument) i.next()).getName() + "\"");
            if (i.hasNext())
            {
                m_writer.print(",");
            }
            m_writer.println();
        }
        m_writer.closeBlock(";");
    }



    private void generateMakeRenderer()
        throws IOException
    {
        m_writer.print(  "public " + ClassNames.RENDERER + " makeRenderer(");
        m_analyzer.getUnitInfo().printRequiredArgsDecl(m_writer);
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
        m_analyzer.getUnitInfo().printRequiredArgs(m_writer);
        m_writer.println(");");
        m_writer.closeBlock();
        m_writer.closeBlock(";");
        m_writer.closeBlock();
        m_writer.println();
    }



    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = m_analyzer.getUnitInfo().getOptionalArgs();
             i.hasNext();
             /* */)
        {
            m_writer.println();
            Argument arg = (Argument) i.next();
            String name = arg.getName();
            m_writer.print("public ");
            String pkgName = getPackageName();
            if (pkgName.length() > 0)
            {
                m_writer.print(pkgName + ".");
            }
            m_writer.print(getClassName());
            m_writer.println(" set" + StringUtils.capitalize(name)
                             + "("
                             + arg.getType() +" p_" + name
                             + ")");
            m_writer.print  ("  throws ");
            m_writer.println(ClassNames.IOEXCEPTION);
            m_writer.openBlock();
            m_writer.println("getInstance().set" + StringUtils.capitalize(name)
                             + "(" + "p_" + name + ");");
            m_writer.println("return this;");
            m_writer.closeBlock();
        }
    }

    private void generateSignature()
        throws IOException
    {
        m_writer.print("public static final String SIGNATURE = \"");
        m_writer.print(m_analyzer.getSignature());
        m_writer.println("\";");
    }

    private void generateIntf()
        throws IOException
    {
        m_writer.println("public interface Intf");
        m_writer.println("  extends " + ClassNames.TEMPLATE_INTF);
        m_writer.openBlock();

        generateFargInterfaces(true);

        m_writer.print("void render(");
        m_analyzer.getUnitInfo().printRequiredArgsDecl(m_writer);
        m_writer.println(")");
        m_writer.println("  throws java.io.IOException;");
        m_writer.println();
        for (Iterator i = m_analyzer.getUnitInfo().getOptionalArgs();
             i.hasNext();
             /* */)
        {
            m_writer.println();
            Argument arg = (Argument) i.next();
            m_writer.println("void set" + StringUtils.capitalize(arg.getName())
                             + "("
                             + arg.getType() + " " + arg.getName()
                             + ");");
        }
        m_writer.closeBlock();

    }


    private void generateGetInstance()
        throws IOException
    {
        m_writer.println();
        m_writer.println("protected Intf getInstance()");
        m_writer.println("  throws " + ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.println("return (Intf) getInstance(\"" + getPath() + "\");");
        m_writer.closeBlock();
    }

    private void generateSetWriter()
        throws IOException
    {
        m_writer.println();
        m_writer.println("public " + getClassName()
                         + " writeTo(java.io.Writer p_writer)");
        m_writer.print  ("  throws ");
        m_writer.println(ClassNames.IOEXCEPTION);
        m_writer.openBlock();
        m_writer.println("getInstance().writeTo(p_writer);");
        m_writer.println("return this;");
        m_writer.closeBlock();
    }

    private void generateEscaping()
        throws IOException
    {
        m_writer.println();
        m_writer.println("public " + getClassName()
                         + " escaping(org.jamon.escaping.Escaping p_escaping)");
        m_writer.openBlock();
        m_writer.println("escape(p_escaping);");
        m_writer.println("return this;");
        m_writer.closeBlock();
    }

    private void generateEpilogue()
        throws IOException
    {
        m_writer.println();
        m_writer.closeBlock();
    }
}
