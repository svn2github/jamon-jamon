package org.modusponens.jtt;

import java.util.*;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class Phase1Generator extends BaseGenerator
{
    private final static String TEMPLATE =
        "org.modusponens.jtt.Template";
    private final static String BASE_FACTORY =
        "org.modusponens.jtt.AbstractTemplateFactory";
    private final static String TEMPLATE_MANAGER =
        "org.modusponens.jtt.TemplateManager";

    public Phase1Generator(String p_packageName,String p_className)
    {
        super(p_packageName,p_className);
    }

    public void generateClassSource()
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateFactoryClass();
        generateRender();
        generateOptionalArgs();
        generateEpilogue();
    }

    private void generateFactoryClass()
    {
        println("  public static class Factory");
        print  ("    extends ");
        println(BASE_FACTORY);
        println("  {");
        print  ("    public ");
        print  (getClassName());
        println(" getInstance(java.io.Writer p_writer)");
        println("    {");
        print  ("      return (");
        print  (getClassName());
        println(") ");
        print  ("        getInstance(\"");
        print  (getFullyQualifiedClassName());
        println("\", p_writer);");
        println("    }");
        println("  }");
        println();
    }

    private void generateDeclaration()
    {
        print("public interface ");
        println(getClassName());
        print("  extends ");
        println(TEMPLATE);
        println("{");
    }
    private void generateRender()
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
    {
        println();
        println("}");
    }
}
