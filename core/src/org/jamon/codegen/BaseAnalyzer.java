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
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jamon.JamonException;
import org.jamon.node.*;
import org.jamon.analysis.AnalysisAdapter;

public class BaseAnalyzer
{
    public BaseAnalyzer(String p_templatePath,
                        TemplateDescriber p_describer,
                        Set p_children)
    {
        m_templateUnit = new TemplateUnit(p_templatePath);
        m_currentUnit = m_templateUnit;
        m_describer = p_describer;
        m_children = p_children;
    }

    public BaseAnalyzer(String p_templatePath, TemplateDescriber p_describer)
    {
        this(p_templatePath, p_describer, new HashSet());
    }

    public TemplateUnit analyze()
        throws IOException
    {
        Start start =
            m_describer.parseTemplate(m_templateUnit.getName());
        try
        {
            preAnalyze(start);
            mainAnalyze(start);
            return m_templateUnit;
        }
        catch (TunnelingException e)
        {
            throw new JamonException(e.getMessage());
        }
    }

    private void preAnalyze(Start p_start)
        throws IOException
    {
        for (Iterator i = ((ATemplate) p_start.getPTemplate())
                 .getComponent().iterator();
             i.hasNext(); )
        {
            ((PComponent) i.next()).apply(new PreliminaryAdapter());
        }

    }

    protected void mainAnalyze(Start p_start)
        throws IOException
    {
        p_start.apply(new Adapter());
    }

    private final void pushDefUnit(String p_defName)
    {
        DefUnit defUnit = new DefUnit(p_defName, getCurrentUnit());
        m_currentUnit = defUnit;
        getTemplateUnit().addDefUnit(defUnit);
    }

    protected final void pushFragmentUnitImpl(String p_fragName)
    {
        m_currentUnit = new FragmentUnit(p_fragName, getCurrentUnit());
    }

    protected final void popUnit()
    {
        m_currentUnit = m_currentUnit.getParent();
    }

    private String fragmentIntfKey(String p_unitName, String p_fragmentName)
    {
        return p_unitName + "__jamon__" + p_fragmentName;
    }

    protected TemplateUnit getTemplateUnit()
    {
        return m_templateUnit;
    }

    protected AbstractUnit getCurrentUnit()
    {
        return m_currentUnit;
    }

    private final TemplateUnit m_templateUnit;
    private AbstractUnit m_currentUnit;
    private final TemplateDescriber m_describer;
    private final Set m_children;

    private class PreliminaryAdapter extends AnalysisAdapter
    {
        public void caseAExtendsComponent(AExtendsComponent p_extends)
        {
            getTemplateUnit().setParentPath
                (NodeUtils.asText(p_extends.getPath()));
            try
            {
                getTemplateUnit().processParent(m_describer, m_children);
            }
            catch(IOException e)
            {
                throw new TunnelingException(e);
            }
            AUse use = (AUse) p_extends.getUse();
            if (use != null)
            {
                for (Iterator i = use.getParentArg().iterator();
                     i.hasNext(); )
                {
                    AParentArg arg = (AParentArg) i.next();
                    ADefault argDefault = (ADefault) arg.getDefault();
                    getTemplateUnit().addParentArg
                        (arg.getName().getText(),
                         (argDefault != null
                          ? argDefault.getArgexpr().toString().trim()
                          : null));
                }
            }
        }
    }

    protected class Adapter extends AnalysisAdapter
    {
        public void caseStart(Start start)
        {
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

        public void caseAImportsComponent(AImportsComponent imports)
        {
            AImports imps = (AImports) imports.getImports();
            for (Iterator i = imps.getImport().iterator(); i.hasNext(); )
            {
                getTemplateUnit().addImport
                    (NodeUtils.asText(((AImport) i.next()).getName()));
            }
        }

        public void caseAArgsBaseComponent(AArgsBaseComponent p_args)
        {
            handleArgs(((AArgs) p_args.getArgs()).getArg().iterator(),
                       getCurrentUnit());
        }

        public void caseAFargBaseComponent(AFargBaseComponent farg)
        {
            farg.getFarg().apply( new AnalysisAdapter()
                {
                    public void caseAFarg(AFarg p_farg)
                    {
                        String fragmentName =
                            ((AFargStart) p_farg.getFargStart())
                            .getIdentifier().getText();

                        FragmentUnit fragmentUnit =
                            new FragmentUnit(fragmentName, getCurrentUnit());
                        handleFragmentArgs(p_farg.getArg().iterator(),
                                           fragmentUnit);

                        // FIXME: this doesn't handle multiple occurrences of a
                        // frag name (say, in different def components) AT ALL.

                        getCurrentUnit().addFragmentArg(
                            new FragmentArgument(fragmentUnit));
                    }

                    public void caseAArglessFarg(AArglessFarg p_farg)
                    {
                        String fragmentName =
                            ((AFargTag) p_farg.getFargTag())
                            .getIdentifier().getText();
                        FragmentUnit fragmentUnit =
                            new FragmentUnit(fragmentName, getCurrentUnit());
                        getCurrentUnit().addFragmentArg(
                            new FragmentArgument(fragmentUnit));
                    }
                });
        }

        private void handleFragmentArgs(Iterator p_iter,
                                        FragmentUnit p_fragmentUnit)
        {
            while(p_iter.hasNext())
            {
                AArg arg = (AArg) p_iter.next();
                if((ADefault) arg.getDefault() == null)
                {
                    p_fragmentUnit.addRequiredArg(new RequiredArgument(arg));
                }
                else
                {
                    throw new TunnelingException
                        ("farg '" + p_fragmentUnit.getName()
                         + "' has optional argument(s)");
                }
            }
        }

        private void handleArgs(Iterator p_iter,
                                AbstractUnit p_unit)
        {
            while(p_iter.hasNext())
            {
                AArg arg = (AArg) p_iter.next();
                ADefault argDefault = (ADefault) arg.getDefault();
                if(argDefault == null)
                {
                    p_unit.addRequiredArg(new RequiredArgument(arg));
                }
                else
                {
                    p_unit.addOptionalArg
                        (new OptionalArgument(arg, argDefault));
                }
            }
        }

        public void caseADefComponent(ADefComponent node)
        {
            ADef def = (ADef) node.getDef();
            String unitName = def.getIdentifier().getText();
            pushDefUnit(unitName);
            for (Iterator i = def.getBaseComponent().iterator(); i.hasNext(); )
            {
                ((Node)i.next()).apply(this);
            }
            def.getDefEnd().apply(this);
            popUnit();
        }

        public void caseACallBaseComponent(ACallBaseComponent node)
        {
            node.getCall().apply(new PCallAdapter());
        }
    }

    protected class PCallAdapter extends AnalysisAdapter
    {
        public void caseAChildCall(AChildCall p_childCall)
        {
            getTemplateUnit().setIsParent();
        }
    }
}
