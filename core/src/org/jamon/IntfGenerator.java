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
 * Contributor(s):
 */

package org.jamon;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;

public class IntfGenerator
{
    public IntfGenerator(TemplateResolver p_resolver,
                         String p_templatePath,
                         BaseAnalyzer p_analyzer,
                         Writer p_writer)
        throws IOException
    {
        m_path = p_templatePath;
        m_resolver = p_resolver;
        m_analyzer = p_analyzer;
        m_writer = new PrintWriter(p_writer);
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

    private final static String TEMPLATE =
        AbstractTemplateProxy.class.getName();
    private final static String TEMPLATE_INTF =
        TEMPLATE + ".Intf";
    private final static String TEMPLATE_MANAGER =
        TemplateManager.class.getName();
    private final static String IOEXCEPTION_CLASS =
        IOException.class.getName();
    private final static String WRITER_CLASS =
        Writer.class.getName();
    private final static String RENDERER_CLASS =
        Renderer.class.getName();

    private final BaseAnalyzer m_analyzer;
    private final PrintWriter m_writer;
    private final String m_path;
    private final TemplateResolver m_resolver;

    private void print(Object p_obj)
        throws IOException
    {
        m_writer.print(p_obj);
    }

    private void println()
        throws IOException
    {
        m_writer.println();
    }

    private void println(Object p_obj)
        throws IOException
    {
        m_writer.println(p_obj);
    }

    private String getPath()
    {
        return m_path;
    }

    private void generateImports()
        throws IOException
    {
        for (Iterator i = m_analyzer.getImports(); i.hasNext(); /* */ )
        {
            print("import ");
            print(i.next());
            println(";");
        }
        println();
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
            print("package ");
            print(pkgName);
            println(";");
            println();
        }
    }


    private void generateConstructor()
        throws IOException
    {
        println();
        print  ("  public ");
        print  (getClassName());
        print  ("(");
        print  (TEMPLATE_MANAGER);
        println(" p_manager)");
        println("  {");
        println("    super(p_manager);");
        println("  }");
    }



    private void generateFargInterface(FargInfo p_fargInfo, boolean p_inner)
        throws IOException
    {
        print  ("  public static interface Fragment_");
        println(p_fargInfo.getName());
        if (!p_inner)
        {
            print  (" extends ");
            print  (getClassName());
            print  (".Intf.Fragment_");
            print  (p_fargInfo.getName());
            println("{ }");
        }
        else
        {
            print  (" extends org.jamon.AbstractTemplateProxy.Intf");
            println("  {");
            print  ("    void render(");
            for (Iterator a = p_fargInfo.getArgumentNames(); a.hasNext(); /* */)
            {
                String argName = (String) a.next();
                print(p_fargInfo.getArgumentType(argName));
                print(" ");
                print(argName);
                if (a.hasNext())
                {
                    print(", ");
                }
            }
            println(")");
            print  ("      throws ");
            print  (IOEXCEPTION_CLASS);
            println(";");
            println("  }");
        }
        println("");
    }

    private void generateFargInterfaces(boolean p_inner)
        throws IOException
    {
        for (Iterator f = m_analyzer.getFargNames(); f.hasNext(); /* */)
        {
            generateFargInterface(m_analyzer.getFargInfo((String)f.next()),
                                  p_inner);
        }
        println();
    }

    private void generateFargInfo()
        throws IOException
    {
        println("  public static final String[] FARGNAMES = {");
        for (Iterator f = m_analyzer.getFargNames(); f.hasNext(); /* */)
        {
            print("    \"");
            print((String)f.next());
            print("\"");
            if (f.hasNext())
            {
                println(",");
            }
            else
            {
                println();
            }
        }
        println("  };");
        println();
        for (Iterator f = m_analyzer.getFargNames(); f.hasNext(); /* */)
        {
            String name = (String)f.next();
            print("  public static final java.util.Map FARGINFO_");
            print(name);
            println(" = new java.util.HashMap();");
            print("  public static class init_");
            print(name);
            println(" {");
            println("    static {");
            FargInfo info = m_analyzer.getFargInfo(name);
            for (Iterator a = info.getArgumentNames(); a.hasNext(); /* */)
            {
                print("    FARGINFO_");
                print(name);
                String an = (String) a.next();
                print(".put(\"");
                print(an);
                print("\",\"");
                print(info.getArgumentType(an));
                println("\");");
            }
            println("    }");
            println("  }");
            print("  public static final init_");
            print(name);
            print(" init2_");
            print(name);
            print(" = new init_");
            print(name);
            println("();");
        }
        println();
    }


    private void generateDeclaration()
        throws IOException
    {
        print("public class ");
        println(getClassName());
        print("  extends ");
        println(TEMPLATE);
        println("{");
    }

    private void generateRender()
        throws IOException
    {
        print("  public void render(");
        for (Iterator i = m_analyzer.getRequiredArgNames(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print(m_analyzer.getArgType(name));
            print(" p_");
            print(name);
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(")");

        println("    throws java.io.IOException");
        println("  {");
        println("    try");
        println("    {");
        print  ("      getInstance().render(");
        for (Iterator i = m_analyzer.getRequiredArgNames(); i.hasNext(); /* */)
        {
            print("p_");
            print((String) i.next());
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(");");
        println("    }");
        println("    finally");
        println("    {");
        println("      releaseInstance();");
        println("    }");
        println("  }");


        println();
        println("  public static final String[] REQUIRED_ARGS = {");
        for (Iterator i = m_analyzer.getRequiredArgNames(); i.hasNext(); /* */)
        {
            print("    \"");
            print((String) i.next());
            if (i.hasNext())
            {
                println("\",");
            }
            else
            {
                println("\"");
            }
        }
        println("  };");
    }



    private void generateMakeRenderer()
        throws IOException
    {
        print(  "  public ");
        print(  RENDERER_CLASS);
        print(" makeRenderer(");
        for (Iterator i = m_analyzer.getRequiredArgNames(); i.hasNext(); /* */)
        {
            print("final ");
            String name = (String) i.next();
            print(m_analyzer.getArgType(name));
            print(" p_");
            print(name);
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(")");

        println("  {");
        print(  "    return new ");
        print(RENDERER_CLASS);
        println("() {");
        print(  "      public void renderTo(");
        print(  WRITER_CLASS);
        println(" p_writer)");
        print(  "        throws ");
        println(IOEXCEPTION_CLASS);
        println("      {");
        println("        writeTo(p_writer);");
        print  ("        render(");
        for (Iterator i = m_analyzer.getRequiredArgNames(); i.hasNext(); /* */)
        {
            print("p_");
            print((String) i.next());
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(");");
        println("      }");
        println("    };");
        println("  }");
        println();
    }



    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = m_analyzer.getOptionalArgNames(); i.hasNext(); /* */)
        {
            println();
            String name = (String) i.next();
            print("  public ");
            String pkgName = getPackageName();
            if (pkgName.length() > 0)
            {
                print(pkgName);
                print(".");
            }
            print(getClassName());
            print(" set");
            print(StringUtils.capitalize(name));
            print("(");
            print(m_analyzer.getArgType(name));
            print(" p_");
            print(name);
            println(")");
            print  ("    throws ");
            println(IOEXCEPTION_CLASS);
            println("  {");
            print  ("    getInstance().set");
            print(StringUtils.capitalize(name));
            print("(");
            print(" p_");
            print(name);
            println(");");
            println("    return this;");
            println("  }");
        }
    }

    private void generateSignature()
        throws IOException
    {
        print("  public static final String SIGNATURE = \"");
        print(m_analyzer.getSignature());
        println("\";");
    }

    private void generateIntf()
        throws IOException
    {
        println("  public interface Intf");
        print  ("    extends ");
        println(TEMPLATE_INTF);
        println("  {");

        generateFargInterfaces(true);

        print  ("    void render(");
        for (Iterator i = m_analyzer.getRequiredArgNames(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print  (m_analyzer.getArgType(name));
            print  (" p_");
            print  (name);
            if (i.hasNext())
            {
                print  (", ");
            }
        }
        println(")");
        println("      throws java.io.IOException;");
        println();
        for (Iterator i = m_analyzer.getOptionalArgNames(); i.hasNext(); /* */)
        {
            println();
            String name = (String) i.next();
            print  ("   void set");
            print  (StringUtils.capitalize(name));
            print  ("(");
            print  (m_analyzer.getArgType(name));
            print  (" p_");
            print  (name);
            println(");");
        }
        println("  }");

    }


    private void generateGetInstance()
        throws IOException
    {
        println();
        println("  protected Intf getInstance()");
        print  ("    throws ");
        println(IOEXCEPTION_CLASS);
        println("  {");
        print  ("    return (Intf) getInstance(\"");
        print  (getPath());
        println("\");");
        println("  }");
    }

    private void generateSetWriter()
        throws IOException
    {
        println();
        print  ("  public ");
        print  (getClassName());
        println(" writeTo(java.io.Writer p_writer)");
        print  ("    throws ");
        println(IOEXCEPTION_CLASS);
        println("  {");
        println("    getInstance().writeTo(p_writer);");
        println("    return this;");
        println("  }");
    }

    private void generateEscaping()
        throws IOException
    {
        println();
        print  ("  public ");
        print  (getClassName());
        println(" escaping(org.jamon.Escaping p_escaping)");
        println("  {");
        println("    escape(p_escaping);");
        println("    return this;");
        println("  }");
    }

    private void generateEpilogue()
        throws IOException
    {
        println();
        println("}");
    }
}
