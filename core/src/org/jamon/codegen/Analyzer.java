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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jamon.JamonException;
import org.jamon.node.*;
import org.jamon.analysis.AnalysisAdapter;
import org.jamon.analysis.DepthFirstAdapter;

public class Analyzer
{
    public Analyzer(String p_templatePath,
                    TemplateDescriber p_describer,
                    Set p_children)
    {
        m_templateUnit = new TemplateUnit(p_templatePath);
        m_templateDir =
            p_templatePath.substring(0,1 + p_templatePath.lastIndexOf('/'));
        m_currentUnit = m_templateUnit;
        m_describer = p_describer;
        m_children = p_children;
    }

    public Analyzer(String p_templatePath, TemplateDescriber p_describer)
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
        preAnalyze(p_start, new AliasAdapter());
        preAnalyze(p_start, new PreliminaryAdapter());
    }

    private void preAnalyze(Start p_start, AnalysisAdapter p_adapter)
        throws IOException
    {
        for (Iterator i = ((ATemplate) p_start.getPTemplate())
                 .getComponent().iterator();
             i.hasNext(); )
        {
            ((PComponent) i.next()).apply(p_adapter);
        }

    }

    private void mainAnalyze(Start p_start)
        throws IOException
    {
        p_start.apply(new Adapter());
    }

    private void pushDefUnit(String p_defName)
    {
        m_currentUnit = getTemplateUnit().getDefUnit(p_defName);
    }

    private void pushMethodUnit(String p_methodName)
    {
        m_currentUnit = getTemplateUnit().getMethodUnit(p_methodName);
    }

    private void pushOverriddenMethodUnit(String p_methodName)
    {
        m_currentUnit = getTemplateUnit()
            .makeOverridenMethodUnit(p_methodName);
    }

    private FragmentUnit pushFragmentUnitImpl(String p_fragName)
    {
        m_currentUnit = new FragmentUnit(p_fragName, getCurrentUnit());
        return (FragmentUnit) m_currentUnit;
    }

    private void pushFragmentArg(String p_fragName)
    {
        FragmentUnit frag = new FragmentUnit(p_fragName, getCurrentUnit());
        m_currentUnit.addFragmentArg(new FragmentArgument(frag));
        m_currentUnit = frag;
    }

    private void popUnit()
    {
        m_currentUnit = m_currentUnit.getParent();
    }

    private void pushCallStatement(CallStatement p_callStatement)
    {
        m_callStatements.add(p_callStatement);
    }

    private void popCallStatement()
    {
        m_callStatements.removeLast();
    }

    private CallStatement getCurrentCallStatement()
    {
        return (CallStatement) m_callStatements.getLast();
    }

    private String fragmentIntfKey(String p_unitName, String p_fragmentName)
    {
        return p_unitName + "__jamon__" + p_fragmentName;
    }

    private TemplateUnit getTemplateUnit()
    {
        return m_templateUnit;
    }

    private Unit getCurrentUnit()
    {
        return m_currentUnit;
    }

    private StringBuffer m_current = new StringBuffer();
    private final TemplateUnit m_templateUnit;
    private Unit m_currentUnit;
    private final TemplateDescriber m_describer;
    private final Set m_children;
    private final Set m_defNames = new HashSet();
    private final LinkedList m_callStatements = new LinkedList();
    private final Map m_aliases = new HashMap();
    private final String m_templateDir;

    private String getAbsolutePath(String p_path)
    {
        return p_path.charAt(0) == '/'
            ? p_path
            : m_templateDir + p_path;
    }

    private String computePath(PPath p_path)
    {
        PathAdapter adapter = new PathAdapter();
        p_path.apply(adapter);
        return adapter.getPath();
    }

    private class PathAdapter extends DepthFirstAdapter
    {
        private final StringBuffer m_path = new StringBuffer();

        public String getPath()
        {
            return m_path.toString();
        }

        public void inAUpdirPath(AUpdirPath p_updir)
        {
            if (! m_path.toString().startsWith("/"))
            {
                m_path.append(m_templateDir);
            }
            int lastSlash = m_path.toString()
                .lastIndexOf("/", m_path.toString().length() - 2 );
            if (lastSlash == 0)
            {
                throw new TunnelingException
                    ("Cannot reference templates above the root");
            }
            m_path.delete(lastSlash+1, m_path.length());
        }

        public void inARelativePath(ARelativePath p_relativePath)
        {
            m_path.append(p_relativePath.getIdentifier().getText());
        }

        public void inAAbsolutePath(AAbsolutePath p_absPath)
        {
            m_path.append("/");
        }

        public void inAAliasedPath(AAliasedPath p_aliasedPath)
        {
            String alias = p_aliasedPath.getIdentifier() == null
                ? "/"
                : p_aliasedPath.getIdentifier().getText().trim();
            String prefix = (String) m_aliases.get(alias);
            if (prefix == null)
            {
                throw new TunnelingException("Unknown alias " + alias);
            }
            else
            {
                m_path.append(prefix);
            }
        }
    }

    private class AliasAdapter extends AnalysisAdapter
    {
        private void addAlias(String p_name, String p_path)
        {
            if (m_aliases.containsKey(p_name))
            {
                throw new TunnelingException("Duplicate alias " + p_name);
            }
            else
            {
                m_aliases.put(p_name, p_path);
            }
        }

        public void caseAAliasComponent(AAliasComponent p_alias)
        {
            for (Iterator a = ((AAliases)p_alias.getAliases())
                     .getAlias().iterator();
                 a.hasNext(); )
            {
                AAlias alias = (AAlias) a.next();
                addAlias(alias.getAliasName().toString().trim(),
                         computePath(alias.getPath()));
            }
        }
    }

    private class PreliminaryAdapter extends AnalysisAdapter
    {
        public void caseAInheritsComponent(AInheritsComponent p_inherits)
        {
            getTemplateUnit().setParentPath
                (getAbsolutePath(computePath(p_inherits.getPath())));
            try
            {
                getTemplateUnit().processParent(m_describer, m_children);
            }
            catch(IOException e)
            {
                throw new TunnelingException(e);
            }
        }

        public void caseADefComponent(ADefComponent p_def)
        {
            getTemplateUnit().
                makeDefUnit(((ADef) p_def.getDef()).getIdentifier().getText());
        }

        public void caseAMethodComponent(AMethodComponent p_method)
        {
            getTemplateUnit().
                makeMethodUnit(((AMethod) p_method.getMethod())
                               .getIdentifier().getText());
        }
    }

    private class Adapter extends DepthFirstAdapter
    {
        public void caseAImport(AImport p_import)
        {
            getTemplateUnit().addImport(p_import.getName().toString());
        }

        public void caseAImplement(AImplement p_implement)
        {
            getTemplateUnit().addInterface(p_implement.getName().toString());
        }

        public void caseAParentArg(AParentArg p_arg)
        {
            ADefault argDefault = (ADefault) p_arg.getDefault();
            ((InheritedUnit) getCurrentUnit()).addParentArg
                (p_arg.getName().getText(),
                 (argDefault != null
                  ? argDefault.getArgexpr().toString().trim()
                  : null));
        }

        public void caseAFargStart(AFargStart p_fargStart)
        {
            pushFragmentArg(p_fargStart.getIdentifier().getText());
        }

        public void outAFarg(AFarg node)
        {
            popUnit();
        }

        public void caseAArglessFarg(AArglessFarg p_farg)
        {
            String fragmentName = ((AFargTag) p_farg.getFargTag())
                .getIdentifier().getText();
            FragmentUnit fragmentUnit =
                new FragmentUnit(fragmentName, getCurrentUnit());
            getCurrentUnit().addFragmentArg(
                new FragmentArgument(fragmentUnit));
        }

        public void caseAArg(AArg p_arg)
        {
            ADefault argDefault = (ADefault) p_arg.getDefault();
            if(argDefault == null)
            {
                getCurrentUnit().addRequiredArg(new RequiredArgument(p_arg));
            }
            else
            {
                getCurrentUnit().addOptionalArg(
                    new OptionalArgument(p_arg, argDefault));
            }
        }

        public void inADef(ADef p_def)
        {
            handleBody();
            pushDefUnit(p_def.getIdentifier().getText());
        }

        public void outADef(ADef p_def)
        {
            handleBody();
            popUnit();
        }

        public void inAMethod(AMethod p_method)
        {
            handleBody();
            pushMethodUnit(p_method.getIdentifier().getText());
        }

        public void outAMethod(AMethod p_method)
        {
            handleBody();
            popUnit();
        }

        public void inAOverride(AOverride p_override)
        {
            handleBody();
            pushOverriddenMethodUnit(p_override.getIdentifier().getText());
        }

        public void outAOverride(AOverride p_override)
        {
            handleBody();
            popUnit();
        }

        public void inACallBaseComponent(ACallBaseComponent node)
        {
            handleBody();
        }

        public void caseACall(ACall p_call)
        {
            addStatement
                (makeCallStatement(p_call.getPath(), p_call.getParam()));
        }

        public void caseAChildCall(AChildCall p_childCall)
        {
            TemplateUnit unit = getTemplateUnit();
            unit.setIsParent();
            addStatement(new ChildCallStatement(unit.getInheritanceDepth()+1));
        }

        public void inAClassComponent(AClassComponent node)
        {
            handleBody();
        }

        public void caseTClassContent(TClassContent p_content)
        {
            handleBody();
            getTemplateUnit().addClassContent(p_content.getText());
        }

        public void caseALiteralBaseComponent(ALiteralBaseComponent node)
        {
            handleBody();
            addStatement
                (new LiteralStatement(node.getLiteralText().getText()));
        }

        public void caseABodyBaseComponent(ABodyBaseComponent node)
        {
            m_current.append(node.getText().getText());
        }

        public void caseANewlineBaseComponent(ANewlineBaseComponent node)
        {
            m_current.append(node.getNewline().getText());
        }

        public void inAMultiFragmentCall(AMultiFragmentCall p_call)
        {
            handleBody();
            CallStatement s =
                makeCallStatement(p_call.getPath(), p_call.getParam());
            addStatement(s);
            pushCallStatement(s);
        }

        public void outAMultiFragmentCall(AMultiFragmentCall p_call)
        {
            popCallStatement();
        }

        public void inANamedFarg(ANamedFarg p_farg)
        {
            getCurrentCallStatement().addFragmentImpl(
                pushFragmentUnitImpl(p_farg.getIdentifier().getText()));
        }

        public void outANamedFarg(ANamedFarg p_farg)
        {
            handleBody();
            popUnit();
        }

        public void inAFragmentCall(AFragmentCall p_call)
        {
            handleBody();
            CallStatement s =
                makeCallStatement(p_call.getPath(), p_call.getParam());
            addStatement(s);
            s.addFragmentImpl(pushFragmentUnitImpl(null));
        }

        public void outAFragmentCall(AFragmentCall p_call)
        {
            handleBody();
            popUnit();
        }

        public void caseEOF(EOF node)
        {
            handleBody();
        }

        public void caseAJava(AJava p_java)
        {
            handleBody();
            addStatement(new RawStatement(p_java.getJavaStmts().getText()));
        }

        public void caseAPartialJline(APartialJline node)
        {
            handleBody();
            addStatement(new RawStatement(node.getExpr().getText()));
        }

        public void caseAJline(AJline p_jline)
        {
            handleBody();
            addStatement(new RawStatement(p_jline.getExpr().getText()));
        }

        public void caseAEmit(AEmit p_emit)
        {
            handleBody();
            AEscape escape = (AEscape) p_emit.getEscape();
            EscapingDirective directive =
                escape == null
                ? EscapingDirective.DEFAULT
                : EscapingDirective.get(escape.getEscapeCode().getText());
            addStatement(new WriteStatement(p_emit.getEmitExpr().getText(),
                                            directive));
        }
    }

    private void handleBody()
    {
        if (m_current.length() > 0)
        {
            addStatement(new LiteralStatement(m_current.toString()));
            m_current = new StringBuffer();
        }
    }


    private CallStatement makeCallStatement(PPath p_path, List p_calls)
    {
        String path = computePath(p_path);
        FragmentUnit fragmentUnit =
            getCurrentUnit().getFragmentUnitIntf(path);
        if (fragmentUnit != null)
        {
            return new FargCallStatement
                (path, makeParamMap(p_calls), fragmentUnit);
        }
        else
        {
            DefUnit defUnit = getTemplateUnit().getDefUnit(path);
            if (defUnit != null)
            {
                return new DefCallStatement
                    (path, makeParamMap(p_calls), defUnit);
            }
            else
            {
                MethodUnit methodUnit =
                    getTemplateUnit().getMethodUnit(path);
                if (methodUnit != null)
                {
                    return new MethodCallStatement
                        (path, makeParamMap(p_calls), methodUnit);
                }
                else
                {
                    getTemplateUnit().addCallPath(getAbsolutePath(path));
                    return new ComponentCallStatement(getAbsolutePath(path),
                                                      makeParamMap(p_calls));
                }
            }
        }
    }

    private Map makeParamMap(List p_paramList)
    {
        Map paramMap = new HashMap();
        for (Iterator p = p_paramList.iterator(); p.hasNext(); /* */)
        {
            AParam param = (AParam) p.next();
            paramMap.put(param.getIdentifier().getText(),
                         param.getParamExpr().getText().trim());
        }
        return paramMap;
    }

    private void addStatement(Statement p_statement)
    {
        getCurrentUnit().addStatement(p_statement);
    }
}
