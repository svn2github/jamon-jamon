package org.modusponens.jtt;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class BaseAnalyzer
{
    public BaseAnalyzer(Start p_start)
    {
        p_start.apply(new Adapter());
    }

    protected BaseAnalyzer()
    {
    }

    public List getDefNames()
    {
        return m_defNames;
    }

    public Iterator getImports()
    {
        return m_imports.iterator();
    }

    public Iterator getRequiredArgNames()
    {
        return getRequiredArgNames(MAIN_UNIT_NAME);
    }

    public Iterator getRequiredArgNames(String p_unitName)
    {
        return getUnitInfo(p_unitName).getRequiredArgNames();
    }

    public Iterator getOptionalArgNames()
    {
        return getOptionalArgNames(MAIN_UNIT_NAME);
    }

    public Iterator getOptionalArgNames(String p_unitName)
    {
        return getUnitInfo(p_unitName).getOptionalArgNames();
    }

    public String getArgType(String p_unitName,String p_argName)
    {
        return getUnitInfo(p_unitName).getArgType(p_argName);
    }

    public String getDefault(String p_unitName,String p_argName)
    {
        return getUnitInfo(p_unitName).getDefault(p_argName);
    }

    public String getArgType(String p_argName)
    {
        return getArgType(MAIN_UNIT_NAME,p_argName);
    }

    public String getDefault(String p_argName)
    {
        return getDefault(MAIN_UNIT_NAME,p_argName);
    }

    protected static final String MAIN_UNIT_NAME = "";

    protected final void pushUnitName(String p_unitName)
    {
        m_unitNames.addLast(p_unitName);
    }

    protected final String popUnitName()
    {
        return (String) m_unitNames.removeLast();
    }

    protected String getUnitName()
    {
        return (String) m_unitNames.get(m_unitNames.size()-1);
    }

    private UnitInfo getUnitInfo(String p_unitName)
    {
        return (UnitInfo) m_unit.get(p_unitName);
    }

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
                m_default.put(p_name,
                              p_default.getArgexpr().toString().trim());
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
        private List m_requiredArgs = new LinkedList();
        private List m_optionalArgs = new LinkedList();
    }

    private List m_imports = new LinkedList();
    private Map m_unit = new HashMap();
    private List m_defNames = new LinkedList();
    private final LinkedList m_unitNames = new LinkedList();



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


    protected class Adapter extends AnalysisAdapter
    {

        public void caseStart(Start start)
        {
            m_unitNames.add(MAIN_UNIT_NAME);
            m_unit.put(getUnitName(),new UnitInfo(getUnitName()));
            start.getPTemplate().apply(this);
            start.getEOF().apply(this);
        }

        public void caseAComponent(AComponent node)
        {
            node.getBaseComponent().apply(this);
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
            getUnitInfo(getUnitName()).addArg(arg.getName().getText(),
                                              asText(arg.getType()),
                                              (ADefault)arg.getDefault());
        }

        public void caseAImportsComponent(AImportsComponent imports)
        {
            AImports imps = (AImports) imports.getImports();
            for (Iterator i = imps.getName().iterator(); i.hasNext(); /* */ )
            {
                m_imports.add(asText((PName) i.next()));
            }
        }

        public void caseAArgsBaseComponent(AArgsBaseComponent args)
        {
            AArgs a = (AArgs) args.getArgs();
            for (Iterator i = a.getArg().iterator(); i.hasNext(); /**/ )
            {
                ((Node)i.next()).apply(this);
            }
        }

        public void caseAFargBaseComponent(AFargBaseComponent farg)
        {
            AFarg f = (AFarg) farg.getFarg();
            AFargStart start = (AFargStart) f.getFargStart();

            String pfragName = start.getIdentifier().getText();
            pushUnitName("#PFRAG#" + pfragName);
            m_unit.put(getUnitName(),new UnitInfo(getUnitName()));

            for (Iterator a = f.getArg().iterator(); a.hasNext(); /* */)
            {
                ((Node)a.next()).apply(this);
            }

            if (getRequiredArgNames(getUnitName()).hasNext())
            {
                throw new UnsupportedOperationException("PFrags cannot have optional arguments");
            }

            popUnitName();

            // FIXME: we want to ensure that this is the first argument
            // FIXME: this doesn't handle multiple occurrences AT ALL.

            getUnitInfo(getUnitName())
                .addArg(pfragName,
                        Fragment.class.getName(),
                        null);

        }

        public void caseADefComponent(ADefComponent node)
        {
            ADef def = (ADef) node.getDef();
            String unitName = def.getIdentifier().getText();
            m_defNames.add(unitName);
            pushUnitName(unitName);
            m_unit.put(getUnitName(),new UnitInfo(getUnitName()));
            def.getDefStart().apply(this);
            for (Iterator i = def.getBaseComponent().iterator(); i.hasNext(); /* */ )
            {
                ((Node)i.next()).apply(this);
            }
            def.getDefEnd().apply(this);
            popUnitName();
        }
    }
}
