package org.modusponens.jtt;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;

public class ImplGenerator
{
    public ImplGenerator(Writer p_writer,
                         TemplateResolver p_resolver,
                         TemplateDescriber p_describer,
                         ImplAdapter p_adapter)
    {
        m_writer = new PrintWriter(p_writer);
        m_resolver = p_resolver;
        m_adapter = p_adapter;
        m_describer = p_describer;
    }

    public void generateSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateConstructor();
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
    private final ImplAdapter m_adapter;
    private final PrintWriter m_writer;

    private final String getPath()
    {
        return m_adapter.getPath();
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


    private String getInterfaceClassName()
    {
        return m_resolver.getIntfClassName(getPath());
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
        String pkgName = getPackageName();
        if (pkgName.length() > 0)
        {
            print(pkgName);
            print(".");
        }
        println(              getInterfaceClassName());
        println("{");
    }

    private void generateConstructor()
        throws IOException
    {
        print("  public ");
        print(getClassName());
        print("(");
        print(WRITER_CLASS);
        println(" p_writer,");
        print  ("        ");
        print  (         TEMPLATE_MANAGER);
        println(                        " p_templateManager)");
        println("  {");
        println("    super(p_writer, p_templateManager);");
        println("  }");
        println();

    }

    private String getPackageName()
    {
        return m_resolver.getImplPackageName(getPath());
    }

    private void generatePrologue()
        throws IOException
    {
        if (getPackageName().length() > 0)
        {
            print("package ");
            print(getPackageName());
            println(";");
            println();
        }
    }


    private void generateDefs()
        throws IOException
    {
        for (Iterator d = m_adapter.getDefNames().iterator(); d.hasNext(); /* */)
        {
            String name = (String) d.next();
            println();
            print("  private void $def$");
            print(name);
            print("(");
            int argNum = 0;
            for (Iterator a = m_adapter.getRequiredArgNames(name);
                 a.hasNext();
                 /* */)
            {
                if (argNum++ > 0)
                {
                    print(",");
                }
                String arg = (String) a.next();
                print("final ");
                print(m_adapter.getArgType(name,arg));
                print(" ");
                print(arg);
            }
            for (Iterator a = m_adapter.getOptionalArgNames(name);
                 a.hasNext();
                 /* */)
            {
                if (argNum++ > 0)
                {
                    print(",");
                }
                String arg = (String) a.next();
                print(m_adapter.getArgType(name,arg));
                print(" ");
                print(arg);
            }
            println(")");
            print  ("    throws ");
            println(IOEXCEPTION_CLASS);
            println("  {");
            for (Iterator i = m_adapter.getStatements(name).iterator();
                 i.hasNext();
                 /* */)
            {
                print("    ");
                ((Statement)i.next()).generateSource(m_writer,
                                                     m_resolver,
                                                     m_describer,
                                                     m_adapter);
            }
            println("  }");
            println();
        }
    }

    private static final String TEMPLATE_MANAGER =
        TemplateManager.class.getName();

    private static final String BASE_TEMPLATE =
        AbstractTemplate.class.getName();

    private void generateRender()
        throws IOException
    {
        print("  public void render(");
        for (Iterator i = m_adapter.getRequiredArgNames(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print("final ");
            print(m_adapter.getArgType(name));
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
        for (Iterator i = m_adapter.getStatements().iterator(); i.hasNext(); /* */)
        {
            print("    ");
            ((Statement)i.next()).generateSource(m_writer,
                                                 m_resolver,
                                                 m_describer,
                                                 m_adapter);
        }
        println("  }");
    }

    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = m_adapter.getOptionalArgNames(); i.hasNext(); /* */)
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
            print(getInterfaceClassName());
            print(" set");
            print(StringUtils.capitalize(name));
            print("(");
            String type = m_adapter.getArgType(name);
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
            println("    return this;");
            println("  }");
            println();
            print("  private ");
            print(type);
            print(" ");
            print(name);
            print(" = ");
            print(m_adapter.getDefault(name));
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
        for (Iterator i = m_adapter.getImports(); i.hasNext(); /* */ )
        {
            print("import ");
            print(i.next());
            println(";");
        }
        println();
    }

}
