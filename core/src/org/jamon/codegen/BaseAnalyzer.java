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
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.security.NoSuchAlgorithmException;

import org.jamon.JamonException;
import org.jamon.node.*;
import org.jamon.analysis.*;

public class BaseAnalyzer
{
    public BaseAnalyzer(Start p_start)
        throws IOException
    {
        this();
        try
        {
            p_start.apply(new Adapter());
        }
        catch (TunnelingException e)
        {
            throw new JamonException(e.getMessage());
        }
    }

    protected static class TunnelingException
        extends RuntimeException
    {
        TunnelingException(String p_message)
        {
            super(p_message);
        }
    }

    protected BaseAnalyzer() {}

    public List getDefNames()
    {
        return m_defNames;
    }

    public Iterator getImports()
    {
        return m_imports.iterator();
    }

    protected static final String MAIN_UNIT_NAME = "";

    protected final void pushUnitName(String p_unitName)
    {
        m_unitNames.addLast(p_unitName);
    }

    protected final void pushUnit(String p_unitName)
    {
        pushUnitName(p_unitName);
        m_unit.put(p_unitName,new UnitInfo(p_unitName));
    }

    private final void pushDefUnit(String p_defName)
    {
        pushUnitName(p_defName);
        m_unit.put(p_defName, new DefInfo(p_defName));
    }

    private final void pushFargUnit(String p_fragName)
    {
        String unitName = "#PFRAG#" + p_fragName;
        pushUnitName(unitName);
        m_unit.put(unitName, new FargInfo(p_fragName));
    }

    protected final String popUnitName()
    {
        return (String) m_unitNames.removeLast();
    }

    private boolean isFarg(String p_unitName)
    {
        return m_unit.get(p_unitName) instanceof FargInfo;
    }

    protected String getUnitName()
    {
        return (String) m_unitNames.getLast();
    }

    public DefInfo getDefInfo(String p_defName)
    {
        return (DefInfo) getAbstractUnitInfo(p_defName);
    }

    public FargInfo getFargInfo(String p_fargName)
    {
        return getFargInfo(MAIN_UNIT_NAME, p_fargName);
    }

    protected FargInfo getFargInfo(String p_unitName, String p_fargName)
    {
        //FIXME - use the unitName or get rid of it.
        return (FargInfo) getAbstractUnitInfo("#PFRAG#" + p_fargName);
    }

    public UnitInfo getUnitInfo()
    {
        return (UnitInfo) getUnitInfo(MAIN_UNIT_NAME);
    }

    public AbstractStandardUnitInfo getUnitInfo(String p_unitName)
    {
        return (AbstractStandardUnitInfo) getAbstractUnitInfo(p_unitName);
    }

    public AbstractUnitInfo getAbstractUnitInfo(String p_unitName)
    {
        return (AbstractUnitInfo)m_unit.get(p_unitName);
    }

    private final List m_imports = new LinkedList();
    private final Map m_unit = new HashMap();
    private final List m_defNames = new LinkedList();
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
            PPartialJline finalJline = node.getPartialJline();
            if (finalJline != null)
            {
                finalJline.apply(this);
            }
        }

        public void caseAArg(AArg arg)
        {
            ADefault argDefault = (ADefault) arg.getDefault();
            String name = arg.getName().getText();
            String type = asText(arg.getType());
            if (argDefault == null)
            {
                getAbstractUnitInfo(getUnitName()).addRequiredArg(name, type);
            }
            else if (isFarg(getUnitName()))
            {
                throw new TunnelingException
                    ("farg '"
                     + getAbstractUnitInfo(getUnitName()).getName()
                     + "' has optional argument(s)");
            }
            else
            {
                getUnitInfo(getUnitName())
                    .addOptionalArg(name,
                                    type,
                                    argDefault.getArgexpr().toString().trim());
            }
        }

        public void caseAImportsComponent(AImportsComponent imports)
        {
            AImports imps = (AImports) imports.getImports();
            for (Iterator i = imps.getImport().iterator(); i.hasNext(); /* */ )
            {
                m_imports.add(asText(((AImport) i.next()).getName()));
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
            pushFargUnit(pfragName);

            for (Iterator a = f.getArg().iterator(); a.hasNext(); /* */)
            {
                ((Node)a.next()).apply(this);
            }

            popUnitName();

            // FIXME: this doesn't handle multiple occurrences of a
            // frag name (say, in different def components) AT ALL.

            getUnitInfo(getUnitName())
                .addFarg(pfragName, "Fragment_" + pfragName);

        }

        public void caseADefComponent(ADefComponent node)
        {
            ADef def = (ADef) node.getDef();
            String unitName = def.getIdentifier().getText();
            m_defNames.add(unitName);
            pushDefUnit(unitName);
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
