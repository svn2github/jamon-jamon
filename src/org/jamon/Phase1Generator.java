package org.modusponens.jtt;

import java.util.*;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class Phase1Generator extends AnalysisAdapter
{
    private void print(Object p_obj)
    {
        System.out.print(p_obj);
    }

    private void println()
    {
        System.out.println();
    }

    private void println(Object p_obj)
    {
        System.out.println(p_obj);
    }

    private List m_imports = new ArrayList();
    private Map m_argTypes = new HashMap();
    private List m_requiredArgs = new ArrayList();
    private List m_optionalArgs = new ArrayList();
    private final String m_className;
    private final String m_packageName;

    private final static String BASE_TEMPLATE =
        "org.modusponens.jtt.BaseTemplate";
    private final static String TEMPLATE_MANAGER =
        "org.modusponens.jtt.TemplateManager";

    public Phase1Generator(String p_packageName,String p_className)
    {
        m_packageName = p_packageName;
        m_className = p_className;
    }

    public void caseStart(Start start)
    {
        start.getPTemplate().apply(this);
        generateClassSource();
    }

    private String getFullyQualifiedClassName()
    {
        return m_packageName + "." + m_className;
    }

    private void generateClassSource()
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateFactoryMethod();
        generateRender();
        generateOptionalArgs();
        generateEpilogue();
    }

    private void generateFactoryMethod()
    {
        print("  public ");
        print(m_className);
        println(" getInstance(PrintWriter p_writer)");
        println("  {");
        print("    return (");
        print(m_className);
        println(") ");
        print("      ");
        print(TEMPLATE_MANAGER);
        print(".getInstance(\"");
        print(getFullyQualifiedClassName());
        println("\");");
        println("  }");
        println();
    }

    private void generatePrologue()
    {
        print("package ");
        print(m_packageName);
        println(";");
        println();
    }

    private void generateImports()
    {
        println("import java.io.IOException;");
        println("import java.io.PrintWriter;");
        for (Iterator i = m_imports.iterator(); i.hasNext(); /* */ )
        {
            print("import ");
            print(i.next());
            println(";");
        }
        println();
    }

    private void generateDeclaration()
    {
        print("public abstract class ");
        println(m_className);
        print("  extends ");
        println(BASE_TEMPLATE);
        println("{");
    }
    private void generateRender()
    {
        print("  public void render(");
        for (Iterator i = m_requiredArgs.iterator(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print(m_argTypes.get(name));
            print(" p_");
            print(name);
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(")");

        println("    throws IOException;");
    }

    private void generateOptionalArgs()
    {
        for (Iterator i = m_optionalArgs.iterator(); i.hasNext(); /* */)
        {
            println();
            String name = (String) i.next();
            print("  public void set");
            print(capitalize(name));
            print("(");
            print(m_argTypes.get(name));
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

    private String capitalize(String p_string)
    {
        // assert p_string != null
        char [] chars = p_string.toCharArray();
        if (chars.length == 0)
        {
            return p_string;
        }
        else
        {
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        }
    }

    public void caseATemplate(ATemplate node)
    {
        for (Iterator i = node.getComponent().iterator(); i.hasNext(); /**/ )
        {
            ((Node)i.next()).apply(this);
        }
    }

    public void caseAArg(AArg arg)
    {
        String name = arg.getName().getText();
        if (arg.getDefault() == null)
        {
            m_requiredArgs.add(name);
            m_argTypes.put(name, asText(arg.getType()));
        }
        else
        {
            m_optionalArgs.add(name);
            m_argTypes.put(name,asText(arg.getType()));
        }
    }

    public void caseAImportsComponent(AImportsComponent imports)
    {
        for (Iterator i = imports.getName().iterator(); i.hasNext(); /* */ )
        {
            m_imports.add(asText((PName) i.next()));
        }
    }

    private String asText(PName name)
    {
        if (name instanceof ASimpleName)
        {
            return ((ASimpleName)name).getIdentifier().getText();

        }
        else if (name instanceof AQualifiedName)
        {
            AQualifiedName qname = (AQualifiedName) name;
            return asText(qname.getName())
                + '.'
                + qname.getIdentifier().getText();
        }
        else
        {
            throw new RuntimeException("Unknown name type "
                                       + name.getClass().getName());
        }
    }

    private String asText(PType p_type)
    {
        StringBuffer str = new StringBuffer();
        AType type = (AType) p_type;
        str.append(asText(type.getName()));
        for (Iterator i = type.getBrackets().iterator(); i.hasNext(); i.next())
        {
            str.append("[]");
        }
        return str.toString();
    }

    public void caseAArgsComponent(AArgsComponent args)
    {
        for (Iterator i = args.getArg().iterator(); i.hasNext(); /**/ )
        {
            ((Node)i.next()).apply(this);
        }
    }
}
