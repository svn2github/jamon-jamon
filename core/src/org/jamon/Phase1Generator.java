package org.modusponens.jtt;

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class Phase1Generator extends BaseGenerator
{
    private final static String TEMPLATE =
        Template.class.getName();
    private final static String BASE_FACTORY =
        AbstractTemplateFactory.class.getName();
    private final static String TEMPLATE_MANAGER =
        TemplateManager.class.getName();

    public Phase1Generator(Writer p_writer,
                           String p_packageName,
                           String p_className)
    {
        super(p_writer,p_packageName,p_className);
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
        generateRequiredArgsField();
        generateEpilogue();
    }

    private void generateRequiredArgsField()
        throws IOException
    {
        println();
        println("  public static final String[] RENDER_ARGS = {");
        for (Iterator i = getRequiredArgs(); i.hasNext(); /* */)
        {
            print("    \"");
            print(i.next());
            print("\"");
            println(i.hasNext() ? "," : "");
        }
        println("  };");
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
        print  ("    public ");
        print  (getClassName());
        println(" getInstance(java.io.Writer p_writer)");
        println("    {");
        print  ("      return (");
        print  (getClassName());
        println(") ");
        print  ("        getInstance(\"");
        print  ("/");
        print  (getFullyQualifiedClassName().replace('.','/'));
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
        for (Iterator i = getRequiredArgs(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print(getArgType(name));
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
        for (Iterator i = getOptionalArgs(); i.hasNext(); /* */)
        {
            println();
            String name = (String) i.next();
            print("  public void set");
            print(capitalize(name));
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
