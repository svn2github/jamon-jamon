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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.*;
import org.jamon.node.AnalysisAdapter;
import org.jamon.node.DepthFirstAnalysisAdapter;
import org.jamon.util.StringUtils;

public class Analyzer
{
    public Analyzer(String p_templatePath,
                    TemplateDescriber p_describer,
                    Set p_children)
    {
        m_templateUnit = new TemplateUnit(p_templatePath, m_errors);
        m_templateDir =
            p_templatePath.substring(0,1 + p_templatePath.lastIndexOf('/'));
        m_currentUnit = m_templateUnit;
        m_describer = p_describer;
        m_children = p_children;
        m_templateIdentifier =
            m_describer.getExternalIdentifier(p_templatePath);
    }

    public Analyzer(String p_templatePath, TemplateDescriber p_describer)
    {
        this(p_templatePath, p_describer, new HashSet());
    }

    public TemplateUnit analyze()
        throws IOException
    {
        TopNode top = m_describer.parseTemplate(m_templateUnit.getName());
        preAnalyze(top);
        mainAnalyze(top);
        checkForConcreteness(top);
        if (m_errors.hasErrors())
        {
            throw m_errors;
        }
        return m_templateUnit;
    }

    private void addError(String p_message, Location p_location)
    {
        m_errors.addError(new ParserError(p_location, p_message));
    }

    private void preAnalyze(TopNode p_top)
    {
        topLevelAnalyze(p_top, new AliasAdapter());
        topLevelAnalyze(p_top, new PreliminaryAdapter());
        if (m_defaultEscaping == null)
        {
            m_defaultEscaping = EscapingDirective.DEFAULT_ESCAPE_CODE;
        }
    }

    private void topLevelAnalyze(TopNode p_top, AnalysisAdapter p_adapter)
    {
        for (Iterator i = p_top.getSubNodes(); i.hasNext(); )
        {
            ((AbstractNode) i.next()).apply(p_adapter);
        }

    }

    private void mainAnalyze(TopNode p_top)
    {
        p_top.apply(new Adapter());
    }

    public void checkForConcreteness(TopNode p_top)
    {
        if (! getTemplateUnit().isParent()
            && ! getTemplateUnit().getAbstractMethodNames().isEmpty())
        {
            topLevelAnalyze(p_top, new AnalysisAdapter()
                {
                    public void caseExtendsNode(ExtendsNode p_extends)
                    {
                        StringBuffer message =
                            new StringBuffer("The abstract method(s) ");
                        StringUtils.commaJoin(message,
                                  getTemplateUnit().getAbstractMethodNames()
                                  .iterator());
                        message.append(" have no concrete implementation");
                        addError(message.toString(), p_extends.getLocation());
                    }
                });
        }
    }

    private void pushDefUnit(String p_defName)
    {
        m_currentUnit = getTemplateUnit().getDefUnit(p_defName);
    }

    private void pushMethodUnit(String p_methodName)
    {
        m_currentUnit = getTemplateUnit().getMethodUnit(p_methodName);
    }

    private void pushOverriddenMethodUnit(OverrideNode p_node)
    {
        m_currentUnit = getTemplateUnit()
            .makeOverridenMethodUnit(p_node.getName(), p_node.getLocation());
    }

    private FragmentUnit pushFragmentUnitImpl(String p_fragName)
    {
        m_currentUnit = 
            new FragmentUnit(p_fragName, getCurrentUnit(), m_errors);
        return (FragmentUnit) m_currentUnit;
    }

    private void pushFragmentArg(FragmentUnit p_frag)
    {
        m_currentUnit = p_frag;
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
    private final LinkedList m_callStatements = new LinkedList();
    private final Map m_aliases = new HashMap();
    private final String m_templateDir;
    private final String m_templateIdentifier;
    private Location m_currentTextLocation;
    private ParserErrors m_errors = new ParserErrors();

    private String getAbsolutePath(String p_path)
    {
        return p_path.charAt(0) == '/'
            ? p_path
            : m_templateDir + p_path;
    }

    private String computePath(AbstractPathNode p_path)
    {
        PathAdapter adapter = 
            new PathAdapter(m_templateDir, m_aliases, m_errors);
        p_path.apply(adapter);
        return adapter.getPath();
    }

    private class AliasAdapter extends AnalysisAdapter
    {
        public void caseAliasesNode(AliasesNode p_node)
        {
            for (Iterator i = p_node.getAliass(); i.hasNext(); )
            {
                handleAlias((AliasDefNode) i.next());
            }
        }

        private void handleAlias(AliasDefNode p_node)
        {
            if (m_aliases.containsKey(p_node.getName()))
            {
                addError("Duplicate alias for " + p_node.getName(),
                         p_node.getLocation());
            }
            else
            {
                m_aliases.put(p_node.getName(), computePath(p_node.getPath()));
            }
        }
    }

    private class PreliminaryAdapter extends AnalysisAdapter
    {
        public void caseEscapeDirectiveNode(EscapeDirectiveNode p_escape)
        {
            if (m_defaultEscaping != null)
            {
                addError
                    ("a template cannot specify multiple default escapings",
                     p_escape.getLocation());
            }
            m_defaultEscaping = p_escape.getEscapeCode();
            if (EscapingDirective.get(m_defaultEscaping) == null)
            {
                addError("Unknown escaping directive '" + m_defaultEscaping + "'",
                         p_escape.getLocation());
            }
        }

        public void caseExtendsNode(ExtendsNode p_extends)
        {
            if(getTemplateUnit().hasParentPath())
            {
                addError
                    ("a template cannot extend multiple templates",
                     p_extends.getLocation());
            }
            String parentPath =
                getAbsolutePath(computePath(p_extends.getPath()));
            getTemplateUnit().setParentPath(parentPath);
            if (m_children.contains(parentPath))
            {
                addError(
                    "cyclic inheritance involving " + parentPath,
                    p_extends.getLocation());
            }
            else
            {
                m_children.add(parentPath);
                try
                {
                    getTemplateUnit().setParentDescription(
                        m_describer.getTemplateDescription(
                            parentPath,
                            p_extends.getLocation(),
                            m_templateIdentifier,
                            m_children));
                }
                catch (ParserError e)
                {
                    m_errors.addError(e);
                }
                catch (ParserErrors e)
                {
                    m_errors.addErrors(e);
                }
                catch (IOException e)
                {
                    addError(e.getMessage(), p_extends.getLocation());
                }
            }
        }

        public void caseParentMarkerNode(ParentMarkerNode p_node)
        {
            getTemplateUnit().setIsParent();
        }

        public void caseDefNode(DefNode p_node)
        {
            getTemplateUnit()
                .makeDefUnit(p_node.getName(), p_node.getLocation());
        }

        public void caseMethodNode(MethodNode p_node)
        {
            getTemplateUnit()
                .makeMethodUnit(p_node.getName(), p_node.getLocation(), false);
        }

        public void caseAbsMethodNode(AbsMethodNode p_node)
        {
            getTemplateUnit().makeMethodUnit(
                p_node.getName(), p_node.getLocation(), true);
        }
    }

    private class Adapter extends DepthFirstAnalysisAdapter
    {
        public void caseImportNode(ImportNode p_import)
        {
            getTemplateUnit().addImport(p_import);
        }

        public void caseImplementNode(ImplementNode p_node)
        {
            getTemplateUnit().addInterface(p_node.getName());
        }

        public void caseParentArgsNode(ParentArgsNode p_node)
        {
            if (getCurrentUnit() instanceof TemplateUnit
                && ! ((TemplateUnit) getCurrentUnit()).hasParentPath())
            {
                addError(
                    "xargs may not be declared without extending another template",
                    p_node.getLocation());
            }
            else
            {
                super.caseParentArgsNode(p_node);
            }
        }

        public void caseParentArgNode(ParentArgNode p_node)
        {
            ((InheritedUnit) getCurrentUnit()).addParentArg(p_node);
        }

        public void caseParentArgWithDefaultNode(
            ParentArgWithDefaultNode p_node)
        {
            ((InheritedUnit) getCurrentUnit()).addParentArg(p_node);
        }

        public void inFragmentArgsNode(FragmentArgsNode p_node)
        {
            pushFragmentArg(getCurrentUnit().addFragment(p_node));
        }

        public void outFragmentArgsNode(FragmentArgsNode p_node)
        {
            popUnit();
        }

        public void caseArgNode(ArgNode p_node)
        {
            getCurrentUnit().addRequiredArg(p_node);
        }

        public void caseOptionalArgNode(OptionalArgNode p_node)
        {
            getCurrentUnit().addOptionalArg(p_node);
        }
        
        public void inDefNode(DefNode p_node)
        {
            handleBody();
            pushDefUnit(p_node.getName());
        }

        public void outDefNode(DefNode p_def)
        {
            handleBody();
            popUnit();
        }

        public void inMethodNode(MethodNode p_node)
        {
            handleBody();
            pushMethodUnit(p_node.getName());
        }

        public void outMethodNode(MethodNode p_node)
        {
            handleBody();
            popUnit();
        }

        public void inAbsMethodNode(AbsMethodNode p_node)
        {
            handleBody();
            if (! getTemplateUnit().isParent())
            {
                addError(
                    "Non-abstract templates cannot have abstract methods",
                    p_node.getLocation());
            }
            pushMethodUnit(p_node.getName());
        }

        public void outAbsMethodNode(AbsMethodNode p_node)
        {
            popUnit();
        }

        public void inOverrideNode(OverrideNode p_node)
        {
            handleBody();
            pushOverriddenMethodUnit(p_node);
        }

        public void outOverrideNode(OverrideNode p_node)
        {
            handleBody();
            popUnit();
        }

        public void caseSimpleCallNode(SimpleCallNode p_node)
        {
            handleBody();
            addStatement(makeCallStatement(p_node.getCallPath(),
                                           p_node.getParams(),
                                           p_node.getLocation()));
        }

        public void caseChildCallNode(ChildCallNode p_node)
        {
            handleBody();
            TemplateUnit unit = getTemplateUnit();
            if (! unit.isParent())
            {
                addError(
                    "<& *CHILD &> cannot be called from a template without an <%abstract> tag",
                    p_node.getLocation());
            }
            addStatement(new ChildCallStatement(unit.getInheritanceDepth()+1));
        }

        public void caseClassNode(ClassNode p_node)
        {
            handleBody();
            getTemplateUnit().addClassContent(p_node);
        }

        public void caseTextNode(TextNode p_node)
        {
            if (m_currentTextLocation == null)
            {
                m_currentTextLocation = p_node.getLocation();
            }
            m_current.append(p_node.getText());
        }

        public void inMultiFragmentCallNode(MultiFragmentCallNode p_node)
        {
            handleBody();
            CallStatement s = makeCallStatementWithFragments(
                p_node.getLocation(),
                p_node.getCallPath(),
                p_node.getParams());
            addStatement(s);
            pushCallStatement(s);
        }

        public void outMultiFragmentCallNode(MultiFragmentCallNode p_call)
        {
            popCallStatement();
        }

        public void inNamedFragmentNode(NamedFragmentNode p_node)
        {
            getCurrentCallStatement().addFragmentImpl(
                pushFragmentUnitImpl(p_node.getName()), m_errors);
        }

        public void outNamedFragmentNode(NamedFragmentNode p_node)
        {
            handleBody();
            popUnit();
        }

        public void inFragmentCallNode(FragmentCallNode p_node)
        {
            handleBody();
            CallStatement s =
                makeCallStatementWithFragments(p_node.getLocation(),
                                               p_node.getCallPath(),
                                               p_node.getParams());
            addStatement(s);
            s.addFragmentImpl(pushFragmentUnitImpl(null), m_errors);
        }

        public void outFragmentCallNode(FragmentCallNode p_node)
        {
            handleBody();
            popUnit();
        }

        public void outTopNode(TopNode p_node)
        {
            handleBody();
        }

        public void caseJavaNode(JavaNode p_node)
        {
            handleBody();
            addStatement(new RawStatement(p_node.getJava(),
                                          p_node.getLocation(),
                                          m_templateIdentifier));
        }

        private class EmitAdapter extends AnalysisAdapter
        {
            EscapingDirective m_escapeCode = null;
            public void caseEscapeNode(EscapeNode p_node)
            {
                m_escapeCode = EscapingDirective.get(p_node.getEscapeCode());
                if (m_escapeCode == null)
                {
                    addError("Unknown escaping directive '" 
                             + p_node.getEscapeCode()+ "'",
                             p_node.getLocation());
                }
            }
            
            public void caseDefaultEscapeNode(DefaultEscapeNode p_node)
            {
                m_escapeCode = getDefaultEscaping(); 
            }
            
            public EscapingDirective getEscape(AbstractEscapeNode p_node)
            {
                p_node.apply(this);
                return m_escapeCode;
            }
        }
        
        public void caseEmitNode(EmitNode p_node)
        {
            handleBody();
            addStatement(new WriteStatement
                         (p_node.getEmitExpression(),
                          new EmitAdapter().getEscape(p_node.getEscaping()),
                          p_node.getLocation(),
                          m_templateIdentifier));
        }
    }

    private String m_defaultEscaping;

    private EscapingDirective getDefaultEscaping()
    {
        return EscapingDirective.get(m_defaultEscaping);
    }

    private void handleBody()
    {
        if (m_current.length() > 0)
        {
            addStatement(new LiteralStatement(m_current.toString(),
                                              m_currentTextLocation,
                                              m_templateIdentifier));
            m_current = new StringBuffer();
            m_currentTextLocation = null;
        }
    }

    private CallStatement makeCallStatementWithFragments(
        Location p_location,
        AbstractPathNode p_callPath, 
        AbstractParamsNode p_params)
    {
        return makeCallStatement(p_callPath, p_params, p_location);
    }

    private CallStatement makeCallStatement(AbstractPathNode p_path,
                                            AbstractParamsNode p_params,
                                            Location p_location)
    {
        String path = computePath(p_path);
        FragmentUnit fragmentUnit = getCurrentUnit().getFragmentUnitIntf(path);
        ParamValues paramValues = makeParamValues(p_params);
        if (fragmentUnit != null)
        {
            return new FargCallStatement(path,
                                         paramValues,
                                         fragmentUnit,
                                         p_location,
                                         m_templateIdentifier);
        }
        else
        {
            DefUnit defUnit = getTemplateUnit().getDefUnit(path);
            if (defUnit != null)
            {
                return new DefCallStatement(path,
                                            paramValues,
                                            defUnit,
                                            p_location,
                                            m_templateIdentifier);
            }
            else
            {
                MethodUnit methodUnit =
                    getTemplateUnit().getMethodUnit(path);
                if (methodUnit != null)
                {
                    return new MethodCallStatement(path,
                                                   paramValues,
                                                   methodUnit,
                                                   p_location,
                                                   m_templateIdentifier);
                }
                else
                {
                    getTemplateUnit().addCallPath(getAbsolutePath(path));
                    return new ComponentCallStatement(getAbsolutePath(path),
                                                      paramValues,
                                                      p_location,
                                                      m_templateIdentifier);
                }
            }
        }
    }

    private class ParamsAdapter extends DepthFirstAnalysisAdapter
    {
        public ParamValues getParamValues(AbstractParamsNode p_node)
        {
            p_node.apply(this);
            if (m_paramsList != null)
            {
                return new UnnamedParamValues(
                    m_paramsList, p_node.getLocation());
            }
            else
            {
                return new NamedParamValues(
                    m_paramsMap == null
                    ? Collections.EMPTY_MAP
                    : m_paramsMap,
                    p_node.getLocation());
            }
        }

        public void inNamedParamsNode(NamedParamsNode p_node)
        {
            m_paramsMap = new HashMap();
        }
        
        public void inUnnamedParamsNode(UnnamedParamsNode p_node)
        {
            m_paramsList = new LinkedList();
        }
        
        public void caseNamedParamNode(NamedParamNode p_node)
        {
            m_paramsMap.put(p_node.getName().getName(), 
                            p_node.getValue().getValue());
        }
        
        public void caseParamValueNode(ParamValueNode p_node)
        {
            m_paramsList.add(p_node.getValue());
        }
        
        private Map m_paramsMap = null;
        private List m_paramsList = null;
    }
    
    private ParamValues makeParamValues(AbstractParamsNode p_params)
    {
        return new ParamsAdapter().getParamValues(p_params);
    }

    private void addStatement(Statement p_statement)
    {
        getCurrentUnit().addStatement(p_statement);
    }
}
