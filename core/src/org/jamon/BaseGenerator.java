package org.modusponens.jtt;

import java.util.*;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class BaseGenerator extends AnalysisAdapter
{
    protected void print(Object p_obj)
    {
        System.out.print(p_obj);
    }

    protected void println()
    {
        System.out.println();
    }

    protected void println(Object p_obj)
    {
        System.out.println(p_obj);
    }

    private List m_imports = new ArrayList();
    private Map m_argTypes = new HashMap();
    private List m_requiredArgs = new ArrayList();
    private List m_optionalArgs = new ArrayList();
    private final String m_className;
    private final String m_packageName;


    protected BaseGenerator(String p_packageName,String p_className)
    {
        m_packageName = p_packageName;
        m_className = p_className;
    }

    public void caseStart(Start start)
    {
        start.getPTemplate().apply(this);
        start.getEOF().apply(this);
    }

    protected Iterator getImports()
    {
        return m_imports.iterator();
    }

    protected Iterator getRequiredArgs()
    {
        return m_requiredArgs.iterator();
    }

    protected Iterator getOptionalArgs()
    {
        return m_optionalArgs.iterator();
    }

    protected String getArgType(String p_argName)
    {
        return (String) m_argTypes.get(p_argName);
    }

    protected String getClassName()
    {
        return m_className;
    }

    protected String getPackageName()
    {
        return m_packageName;
    }

    protected String getFullyQualifiedClassName()
    {
        return getPackageName() + "." + getClassName();
    }

    protected String capitalize(String p_string)
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


    protected void generatePrologue()
    {
        print("package ");
        print(getPackageName());
        println(";");
        println();
    }

    protected void generateImports()
    {
        for (Iterator i = getImports(); i.hasNext(); /* */ )
        {
            print("import ");
            print(i.next());
            println(";");
        }
        println();
    }

}
