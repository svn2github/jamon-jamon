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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.*;
import org.jamon.util.StringUtils;

public class Analyzer
{
    public Analyzer(String p_templatePath,
                    TemplateDescriber p_describer,
                    Set<String> p_children)
    {
        m_templateUnit = new TemplateUnit(p_templatePath, m_errors);
        m_templateDir =
            p_templatePath.substring(0,1 + p_templatePath.lastIndexOf('/'));
        m_currentStatementBlock = m_templateUnit;
        m_describer = p_describer;
        m_children = p_children;
        m_templateIdentifier =
            m_describer.getExternalIdentifier(p_templatePath);
    }

    public Analyzer(String p_templatePath, TemplateDescriber p_describer)
    {
        this(p_templatePath, p_describer, new HashSet<String>());
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
        for (AbstractNode node : p_top.getSubNodes())
        {
            node.apply(p_adapter);
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
                    @Override
                    public void caseExtendsNode(ExtendsNode p_extends)
                    {
                        StringBuilder message =
                            new StringBuilder("The abstract method(s) ");
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
        m_currentStatementBlock = getTemplateUnit().getDefUnit(p_defName);
    }

    private void pushMethodUnit(String p_methodName)
    {
        m_currentStatementBlock = getTemplateUnit().getMethodUnit(p_methodName);
    }

    private void pushOverriddenMethodUnit(OverrideNode p_node)
    {
        m_currentStatementBlock = getTemplateUnit()
            .makeOverridenMethodUnit(p_node.getName(), p_node.getLocation());
    }

    private void pushWhileBlock(WhileNode p_node)
    {
        WhileBlock whileBlock = new WhileBlock(p_node.getCondition(),
                                               m_currentStatementBlock,
                                               p_node.getLocation());
        addStatement(whileBlock);
        m_currentStatementBlock = whileBlock;
    }

    private void pushForBlock(ForNode p_node)
    {
        ForBlock forBlock = new ForBlock(p_node.getLoopSpecifier(),
                                         m_currentStatementBlock,
                                         p_node.getLocation());
        addStatement(forBlock);
        m_currentStatementBlock = forBlock;
    }

    private FragmentUnit pushFragmentUnitImpl(String p_fragName)
    {
        m_currentStatementBlock = new FragmentUnit(
            p_fragName,
            getCurrentStatementBlock(),
            getTemplateUnit().getGenericParams(),
            m_errors);
        return (FragmentUnit) m_currentStatementBlock;
    }

    private void pushFragmentArg(FragmentUnit p_frag)
    {
        m_currentStatementBlock = p_frag;
    }

    private void popStatementBlock()
    {
        m_currentStatementBlock = m_currentStatementBlock.getParent();
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
        return m_callStatements.getLast();
    }

    private TemplateUnit getTemplateUnit()
    {
        return m_templateUnit;
    }

    private StatementBlock getCurrentStatementBlock()
    {
        return m_currentStatementBlock;
    }

    private StringBuilder m_current = new StringBuilder();
    private final TemplateUnit m_templateUnit;
    private StatementBlock m_currentStatementBlock;
    private final TemplateDescriber m_describer;
    private final Set<String> m_children;
    private final LinkedList<CallStatement> m_callStatements =
        new LinkedList<CallStatement>();
    private final Map<String, String> m_aliases = new HashMap<String, String>();
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
        @Override
        public void caseAliasesNode(AliasesNode p_node)
        {
            for (AliasDefNode defNode : p_node.getAliass())
            {
                handleAlias(defNode);
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
        @Override
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

        @Override
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

        @Override
        public void caseParentMarkerNode(ParentMarkerNode p_node)
        {
            getTemplateUnit().setIsParent();
        }

        @Override
        public void caseDefNode(DefNode p_node)
        {
            getTemplateUnit()
                .makeDefUnit(p_node.getName(), p_node.getLocation());
        }

        @Override
        public void caseMethodNode(MethodNode p_node)
        {
            getTemplateUnit()
                .makeMethodUnit(p_node.getName(), p_node.getLocation(), false);
        }

        @Override
        public void caseAbsMethodNode(AbsMethodNode p_node)
        {
            getTemplateUnit().makeMethodUnit(
                p_node.getName(), p_node.getLocation(), true);
        }
    }

    private class Adapter extends DepthFirstAnalysisAdapter
    {
        @Override
        public void caseImportNode(ImportNode p_import)
        {
            getTemplateUnit().addImport(p_import);
        }

        @Override
        public void caseImplementNode(ImplementNode p_node)
        {
            getTemplateUnit().addInterface(p_node.getName());
        }

        @Override
        public void caseParentArgsNode(ParentArgsNode p_node)
        {
            if (getCurrentStatementBlock() instanceof TemplateUnit
                && ! ((TemplateUnit) getCurrentStatementBlock()).hasParentPath())
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

        @Override
        public void caseParentArgNode(ParentArgNode p_node)
        {
            ((InheritedUnit) getCurrentStatementBlock()).addParentArg(p_node);
        }

        @Override
        public void caseParentArgWithDefaultNode(
            ParentArgWithDefaultNode p_node)
        {
            ((InheritedUnit) getCurrentStatementBlock()).addParentArg(p_node);
        }

        @Override
        public void inFragmentArgsNode(FragmentArgsNode p_node)
        {
            pushFragmentArg(getCurrentStatementBlock().addFragment(
                p_node, getTemplateUnit().getGenericParams()));
        }

        @Override
        public void outFragmentArgsNode(FragmentArgsNode p_node)
        {
            popStatementBlock();
        }

        @Override
        public void caseArgNode(ArgNode p_node)
        {
            getCurrentStatementBlock().addRequiredArg(p_node);
        }

        @Override
        public void caseOptionalArgNode(OptionalArgNode p_node)
        {
            getCurrentStatementBlock().addOptionalArg(p_node);
        }

        @Override
        public void inDefNode(DefNode p_node)
        {
            handleBody();
            pushDefUnit(p_node.getName());
        }

        @Override
        public void outDefNode(DefNode p_def)
        {
            handleBody();
            popStatementBlock();
        }

        @Override
        public void inMethodNode(MethodNode p_node)
        {
            handleBody();
            pushMethodUnit(p_node.getName());
        }

        @Override
        public void outMethodNode(MethodNode p_node)
        {
            handleBody();
            popStatementBlock();
        }

        @Override
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

        @Override
        public void outAbsMethodNode(AbsMethodNode p_node)
        {
            popStatementBlock();
        }

        @Override
        public void inOverrideNode(OverrideNode p_node)
        {
            handleBody();
            pushOverriddenMethodUnit(p_node);
        }

        @Override
        public void outOverrideNode(OverrideNode p_node)
        {
            handleBody();
            popStatementBlock();
        }

        @Override public void caseGenericsParamNode(GenericsParamNode p_node)
        {
            if (m_templateUnit.isParent())
            {
                addError("<%generics> tag not allowed in abstract templates",
                         p_node.getLocation());
            }
            m_templateUnit.addGenericsParamNode(p_node);
        }

        @Override public void caseSimpleCallNode(SimpleCallNode p_node)
        {
            handleBody();
            addStatement(makeCallStatement(p_node));
        }

        @Override
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

        @Override
        public void caseClassNode(ClassNode p_node)
        {
            handleBody();
            getTemplateUnit().addClassContent(p_node);
        }

        @Override
        public void caseTextNode(TextNode p_node)
        {
            if (m_currentTextLocation == null)
            {
                m_currentTextLocation = p_node.getLocation();
            }
            m_current.append(p_node.getText());
        }

        @Override
        public void inMultiFragmentCallNode(MultiFragmentCallNode p_node)
        {
            handleBody();
            CallStatement s = makeCallStatement(p_node);
            addStatement(s);
            pushCallStatement(s);
        }

        @Override
        public void outMultiFragmentCallNode(MultiFragmentCallNode p_call)
        {
            popCallStatement();
        }

        @Override
        public void inNamedFragmentNode(NamedFragmentNode p_node)
        {
            getCurrentCallStatement().addFragmentImpl(
                pushFragmentUnitImpl(p_node.getName()), m_errors);
        }

        @Override
        public void outNamedFragmentNode(NamedFragmentNode p_node)
        {
            handleBody();
            popStatementBlock();
        }

        @Override
        public void inFragmentCallNode(FragmentCallNode p_node)
        {
            handleBody();
            CallStatement s = makeCallStatement(p_node);
            addStatement(s);
            s.addFragmentImpl(pushFragmentUnitImpl(null), m_errors);
        }

        @Override
        public void outFragmentCallNode(FragmentCallNode p_node)
        {
            handleBody();
            popStatementBlock();
        }

        @Override public void inWhileNode(WhileNode p_node)
        {
            handleBody();
            pushWhileBlock(p_node);
        }

        @Override public void outWhileNode(WhileNode p_node)
        {
            handleBody();
            popStatementBlock();
        }

        @Override public void inForNode(ForNode p_node)
        {
            handleBody();
            pushForBlock(p_node);
        }

        @Override public void outForNode(ForNode p_node)
        {
            handleBody();
            popStatementBlock();
        }

        @Override
        public void outTopNode(TopNode p_node)
        {
            handleBody();
        }

        @Override
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
            @Override
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

            @Override
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

        @Override
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
            m_current = new StringBuilder();
            m_currentTextLocation = null;
        }
    }

    private CallStatement makeCallStatement(AbstractComponentCallNode p_node)
    {
        String path = computePath(p_node.getCallPath());
        ParamValues paramValues = makeParamValues(p_node.getParams());
        FragmentUnit fragmentUnit =
            getCurrentStatementBlock().getFragmentUnitIntf(path);
        List<GenericCallParam> genericParams = p_node.getGenericParams();
        if (fragmentUnit != null)
        {
            if (!genericParams.isEmpty())
            {
                addError("Fragment" + " calls may not have generic params",
                         p_node.getLocation());
            }
            return new FargCallStatement(path,
                                         paramValues,
                                         fragmentUnit,
                                         p_node.getLocation(),
                                         m_templateIdentifier);
        }
        else
        {
            DefUnit defUnit = getTemplateUnit().getDefUnit(path);
            if (defUnit != null)
            {
                if (! genericParams.isEmpty())
                {
                    addError("def " + defUnit.getName() +
                             " is being called with generic parameters",
                             p_node.getLocation());
                }
                return new DefCallStatement(path,
                                            paramValues,
                                            defUnit,
                                            p_node.getLocation(),
                                            m_templateIdentifier);
            }
            else
            {
                MethodUnit methodUnit = getTemplateUnit().getMethodUnit(path);
                if (methodUnit != null)
                {
                    if (! genericParams.isEmpty())
                    {
                        addError("method" + methodUnit.getName() +
                                 " is being called with generic parameters",
                                 p_node.getLocation());
                    }
                    return new MethodCallStatement(path,
                                                   paramValues,
                                                   methodUnit,
                                                   p_node.getLocation(),
                                                   m_templateIdentifier);
                }
                else
                {
                    getTemplateUnit().addCallPath(getAbsolutePath(path));
                    return new ComponentCallStatement(getAbsolutePath(path),
                                                      paramValues,
                                                      p_node.getLocation(),
                                                      m_templateIdentifier,
                                                      genericParams);
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
                return new NamedParamValues(m_paramsMap, p_node.getLocation());
            }
        }

        @Override
        public void inNamedParamsNode(NamedParamsNode p_node)
        {
            m_paramsMap = new HashMap<String, String>();
        }

        @Override
        public void inUnnamedParamsNode(UnnamedParamsNode p_node)
        {
            m_paramsList = new LinkedList<String>();
        }

        @Override
        public void caseNamedParamNode(NamedParamNode p_node)
        {
            m_paramsMap.put(p_node.getName().getName(),
                            p_node.getValue().getValue());
        }

        @Override
        public void caseParamValueNode(ParamValueNode p_node)
        {
            m_paramsList.add(p_node.getValue());
        }

        private Map<String, String> m_paramsMap = null;
        private List<String> m_paramsList = null;
    }

    private ParamValues makeParamValues(AbstractParamsNode p_params)
    {
        return new ParamsAdapter().getParamValues(p_params);
    }

    private void addStatement(Statement p_statement)
    {
        getCurrentStatementBlock().addStatement(p_statement);
    }
}
