package org.modusponens.jtt;

import java.util.*;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class Phase2Generator extends BaseGenerator
{
    private List m_body = new ArrayList();
    private StringBuffer m_current = new StringBuffer();

    public Phase2Generator(String p_packageName,String p_className)
    {
        super(p_packageName,p_className);
    }

    public void generateClassSource()
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateRender();
        generateOptionalArgs();
        generateEpilogue();
    }

    private void generateDeclaration()
    {
        print("class ");
        print(getClassName());
        println("Impl");
        print("  extends ");
        println(BASE_TEMPLATE);
        println("{");
    }

    private static final String BASE_TEMPLATE =
        "org.modusponens.jtt.AbstractTemplate";

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

        println("    throws IOException");
        println("  {");
        // FIXME
        println("  }");
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
            String type = getArgType(name);
            print(type);
            print(" p_");
            print(name);
            println(")");
            println("  {");
            print("    m_");
            print(name);
            print(" = p_");
            print(name);
            println(";");
            println("  }");
            println();
            print("  private ");
            print(type);
            print(" m_");
            print(name);
            println(";");
        }
    }

    private void generateEpilogue()
    {
        println();
        println("}");
    }

}
