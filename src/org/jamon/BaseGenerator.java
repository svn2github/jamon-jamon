package org.modusponens.jtt;

import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class BaseGenerator extends AnalysisAdapter
{
    protected void print(Object p_obj)
        throws IOException
    {
        m_writer.print(p_obj);
    }

    protected void println()
        throws IOException
    {
        m_writer.println();
    }

    protected void println(Object p_obj)
        throws IOException
    {
        m_writer.println(p_obj);
    }


    protected static final String MAIN_UNIT_NAME = "";

    private static class UnitInfo
    {
        UnitInfo(String p_name)
        {
            m_name = p_name;
        }
        private final String m_name;
        String getName()
        {
            return m_name;
        }
        void addArg(String p_name, String p_type, ADefault p_default)
        {
            if (p_default == null)
            {
                m_requiredArgs.add(p_name);
                m_argTypes.put(p_name, p_type);
            }
            else
            {
                m_optionalArgs.add(p_name);
                m_argTypes.put(p_name,p_type);
                m_default.put(p_name, p_default.getFragment().toString().trim());
            }
        }
        String getArgType(String p_argName)
        {
            return (String) m_argTypes.get(p_argName);
        }
        String getDefault(String p_argName)
        {
            return (String) m_default.get(p_argName);
        }
        Iterator getRequiredArgNames()
        {
            return m_requiredArgs.iterator();
        }
        Iterator getOptionalArgNames()
        {
            return m_optionalArgs.iterator();
        }
        private final Map m_default = new HashMap();
        private final Map m_argTypes = new HashMap();
        private List m_requiredArgs = new ArrayList();
        private List m_optionalArgs = new ArrayList();
    }

    private List m_imports = new ArrayList();
    private Map m_unit = new HashMap();
    private List m_defNames = new ArrayList();
    private final String m_className;
    private final String m_packageName;
    private final PrintWriter m_writer;

    protected BaseGenerator(Writer p_writer,
                            String p_packageName,
                            String p_className)
    {
        m_writer = new PrintWriter(p_writer);
        m_packageName = p_packageName;
        m_className = p_className;
    }

    public void caseStart(Start start)
    {
        m_unitName = MAIN_UNIT_NAME;
        m_unit.put(m_unitName,new UnitInfo(m_unitName));
        start.getPTemplate().apply(this);
        start.getEOF().apply(this);
    }

    protected List getDefNames()
    {
        return m_defNames;
    }

    protected String getUnitName()
    {
        return m_unitName;
    }

    protected Iterator getImports()
    {
        return m_imports.iterator();
    }

    private UnitInfo getUnitInfo(String p_unitName)
    {
        return (UnitInfo) m_unit.get(p_unitName);
    }

    protected Iterator getRequiredArgNames(String p_unitName)
    {
        return getUnitInfo(p_unitName).getRequiredArgNames();
    }

    protected Iterator getOptionalArgNames(String p_unitName)
    {
        return getUnitInfo(p_unitName).getOptionalArgNames();
    }

    protected String getArgType(String p_unitName,String p_argName)
    {
        return getUnitInfo(p_unitName).getArgType(p_argName);
    }

    protected String getDefault(String p_unitName,String p_argName)
    {
        return getUnitInfo(p_unitName).getDefault(p_argName);
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
        getUnitInfo(m_unitName).addArg(arg.getName().getText(),
                                       asText(arg.getType()),
                                       (ADefault)arg.getDefault());
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

    private String m_unitName;
    private boolean m_inDef = false;

    public void caseADefComponent(ADefComponent node)
    {
        if (! m_unitName.equals(MAIN_UNIT_NAME))
        {
            throw new RuntimeException("Can't nest def!");
        }

        m_unitName = node.getIdentifier().getText();
        m_defNames.add(m_unitName);
        m_unit.put(m_unitName,new UnitInfo(m_unitName));
        node.getDefStart().apply(this);
        for (Iterator i = node.getComponent().iterator();
             i.hasNext();
             /* */ )
        {
            ((Node)i.next()).apply(this);
        }
        node.getDefEnd().apply(this);
        m_unitName = MAIN_UNIT_NAME;
    }

    protected void generatePrologue()
        throws IOException
    {
        print("package ");
        print(getPackageName());
        println(";");
        println();
    }

    protected void generateImports()
        throws IOException
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
