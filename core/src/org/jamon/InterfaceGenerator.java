package org.modusponens.jtt;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class InterfaceGenerator extends BaseGenerator
{
    private final static String TEMPLATE =
        Template.class.getName();
    private final static String BASE_FACTORY =
        AbstractTemplateFactory.class.getName();
    private final static String TEMPLATE_MANAGER =
        TemplateManager.class.getName();
    private final static String JTT_EXCEPTION =
        JttException.class.getName();

    public InterfaceGenerator(TemplateResolver p_resolver,
                              String p_templatePath,
                              BaseGenerator p_generator,
                              Writer p_writer)
    {
        m_path = p_templatePath;
        m_resolver = p_resolver;
        m_generator = p_generator;
        m_writer = new PrintWriter(p_writer);
    }

    private final BaseGenerator m_generator;
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
        for (Iterator i = m_generator.getImports(); i.hasNext(); /* */ )
        {
            print("import ");
            print(i.next());
            println(";");
        }
        println();
    }

    public void generateClassSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateFactoryClass();
        generateRender();
        generateOptionalArgs();
        generateEpilogue();
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

    private void generateFactoryClass()
        throws IOException
    {
        println("  public static class Factory");
        print  ("    extends ");
        println(BASE_FACTORY);
        println("  {");
        print  ("    public Factory(");
        print  (TEMPLATE_MANAGER);
        println(" p_templateManager)");
        println("    {");
        println("      super(p_templateManager);");
        println("    }");
        println();
        String className;
        print  ("    public ");
        print  (getClassName());
        println(" getInstance(java.io.Writer p_writer)");
        print  ("      throws ");
        println(JTT_EXCEPTION);
        println("    {");
        print  ("      return (");
        print  (getClassName());
        println(") ");
        print  ("        getInstance(\"");
        print  (getPath());
        println("\", p_writer);");
        println("    }");
        println("  }");
        println();
    }

    private void generateDeclaration()
        throws IOException
    {
        print("public interface ");
        println(getClassName());
        print("  extends ");
        println(TEMPLATE);
        println("{");
    }
    private void generateRender()
        throws IOException
    {
        print("  public void render(");
        for (Iterator i = m_generator.getRequiredArgNames(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print(m_generator.getArgType(name));
            print(" p_");
            print(name);
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(")");

        println("    throws java.io.IOException;");
    }

    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = m_generator.getOptionalArgNames(); i.hasNext(); /* */)
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
            print(getArgType(name));
            print(" p_");
            print(name);
            println(");");
        }
    }

    private void generateEpilogue()
        throws IOException
    {
        println();
        println("}");
    }
}
