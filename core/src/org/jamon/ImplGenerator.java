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

public class ImplGenerator
{
    public ImplGenerator(Writer p_writer,
                         TemplateResolver p_resolver,
                         TemplateDescriber p_describer,
                         ImplAnalyzer p_analyzer)
    {
        m_writer = new PrintWriter(p_writer);
        m_resolver = p_resolver;
        m_analyzer = p_analyzer;
        m_describer = p_describer;
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
    }

    private final static String IOEXCEPTION_CLASS =
        IOException.class.getName();

    private final static String WRITER_CLASS =
        Writer.class.getName();

    private final TemplateResolver m_resolver;
    private final TemplateDescriber m_describer;
    private final ImplAnalyzer m_analyzer;
    private final PrintWriter m_writer;

    private final String getPath()
    {
        return m_analyzer.getPath();
    }

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


    private String getClassName()
    {
        return m_resolver.getImplClassName(getPath());
    }

    private void generateDeclaration()
        throws IOException
    {
        print  ("public class ");
        println(              getClassName());
        print  ("  extends ");
        println(           BASE_TEMPLATE);
        print  ("  implements ");
        print  (m_resolver.getFullyQualifiedIntfClassName(getPath()));
        println(".Intf");
        println("{");
        println(m_analyzer.getClassContent());
    }

    private void generateInitialize()
        throws IOException
    {
        println("  protected void initializeDefaultArguments()");
        println("    throws Exception");
        println("  {");
        for (Iterator i = m_analyzer.getOptionalArgNames(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print("    ");
            print(name);
            print(" = ");
            print(m_analyzer.getDefault(name));
            println(";");
        }
        println("  }");
        println();
    }

    private void generateConstructor()
        throws IOException
    {
        print  ("  public ");
        print  (          getClassName());
        print  (                        "(");
        print  ( TEMPLATE_MANAGER);
        println(                 " p_templateManager, String p_path)");
        println("  {");
        println("    super(p_templateManager, p_path);");
        println("  }");
        println();
    }

    private void generatePrologue()
        throws IOException
    {
        String pkgName = m_resolver.getImplPackageName(getPath());
        if (pkgName.length() > 0)
        {
            print("package ");
            print(pkgName);
            println(";");
            println();
        }
    }


    private void generateDefFargInterface(FargInfo p_fargInfo)
        throws IOException
    {
        print  ("  private static interface ");
        println(p_fargInfo.getFargInterfaceName());
        println("    extends org.jamon.AbstractTemplateProxy.Intf");
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
        println("      throws java.io.IOException;");
        println("  }");
        println();
    }


    private void generateDefs()
        throws IOException
    {
        for (Iterator d = m_analyzer.getDefNames().iterator(); d.hasNext(); /* */)
        {
            String name = (String) d.next();
            println();

            for (Iterator f = m_analyzer.getFargNames(name);
                 f.hasNext();
                 /* */)
            {
                generateDefFargInterface(m_analyzer.getFargInfo((String)f.next()));
            }

            print("  private void __jamon_def__");
            print(name);
            print("(");
            int argNum = 0;
            for (Iterator a = m_analyzer.getRequiredArgNames(name);
                 a.hasNext();
                 /* */)
            {
                if (argNum++ > 0)
                {
                    print(",");
                }
                String arg = (String) a.next();
                print("final ");
                print(m_analyzer.getArgType(name,arg));
                print(" ");
                print(arg);
            }
            for (Iterator a = m_analyzer.getOptionalArgNames(name);
                 a.hasNext();
                 /* */)
            {
                if (argNum++ > 0)
                {
                    print(",");
                }
                String arg = (String) a.next();
                print(m_analyzer.getArgType(name,arg));
                print(" ");
                print(arg);
            }
            println(")");
            print  ("    throws ");
            println(IOEXCEPTION_CLASS);
            println("  {");
            for (Iterator i = m_analyzer.getStatements(name).iterator();
                 i.hasNext();
                 /* */)
            {
                print("    ");
                ((Statement)i.next()).generateSource(m_writer,
                                                     m_resolver,
                                                     m_describer,
                                                     m_analyzer);
            }
            println("  }");
            println();
        }
    }

    private static final String TEMPLATE_MANAGER =
        TemplateManager.class.getName();

    private static final String BASE_TEMPLATE =
        AbstractTemplateImpl.class.getName();

    private void generateRender()
        throws IOException
    {
        print("  public void render(");
        for (Iterator i = m_analyzer.getRequiredArgNames(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print("final ");
            print(m_analyzer.getArgType(name));
            print(" ");
            print(name);
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(")");

        print  ("    throws ");
        println(IOEXCEPTION_CLASS);
        println("  {");
        for (Iterator i = m_analyzer.getStatements().iterator(); i.hasNext(); /* */)
        {
            print("    ");
            ((Statement)i.next()).generateSource(m_writer,
                                                 m_resolver,
                                                 m_describer,
                                                 m_analyzer);
        }
        println("  }");
    }

    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = m_analyzer.getOptionalArgNames(); i.hasNext(); /* */)
        {
            println();
            String name = (String) i.next();
            print("  public void set");
            print(StringUtils.capitalize(name));
            print("(");
            String type = m_analyzer.getArgType(name);
            print(type);
            print(" p_");
            print(name);
            println(")");
            println("  {");
            print("    ");
            print(name);
            print(" = p_");
            print(name);
            println(";");
            println("  }");
            println();
            print("  private ");
            print(type);
            print(" ");
            print(name);
            println(";");
        }
    }

    private void generateEpilogue()
        throws IOException
    {
        println();
        println("}");
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

}
