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

import org.jamon.JamonTemplateException;
import org.jamon.node.*;
import org.jamon.analysis.AnalysisAdapter;
import org.jamon.analysis.DepthFirstAdapter;
import org.jamon.util.StringUtils;

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
        Start start = m_describer.parseTemplate(m_templateUnit.getName());
        try
        {
            preAnalyze(start);
            mainAnalyze(start);
            checkForConcreteness(start);
            return m_templateUnit;
        }
        catch (TunnelingException e)
        {
            if(e.getRootCause() != null)
            {
                throw e.getRootCause();
            }
            else
            {
                throw new AnalysisException(e.getMessage(),
                                            m_templateIdentifier,
                                            e.getToken());
            }
        }
    }

    private void preAnalyze(Start p_start)
    {
        topLevelAnalyze(p_start, new AliasAdapter());
        topLevelAnalyze(p_start, new PreliminaryAdapter());
        if (m_defaultEscaping == null)
        {
            m_defaultEscaping = EscapingDirective.DEFAULT_ESCAPE_CODE;
        }
    }

    private void topLevelAnalyze(Start p_start, AnalysisAdapter p_adapter)
    {
        for (Iterator i = ((ATemplate) p_start.getPTemplate())
                 .getComponent().iterator();
             i.hasNext(); )
        {
            ((PComponent) i.next()).apply(p_adapter);
        }

    }

    private void mainAnalyze(Start p_start)
    {
        p_start.apply(new Adapter());
    }

    public void checkForConcreteness(Start p_start)
    {
        if (! getTemplateUnit().isParent()
            && ! getTemplateUnit().getAbstractMethodNames().isEmpty())
        {
            topLevelAnalyze(p_start, new AnalysisAdapter()
                {
                    public void caseAExtendsComponent(
                        AExtendsComponent p_extends)
                    {
                        StringBuffer message =
                            new StringBuffer("The abstract method(s) ");
                        StringUtils.commaJoin(message,
                                  getTemplateUnit().getAbstractMethodNames()
                                  .iterator());
                        message.append(" have no concrete implementation");
                        throw new TunnelingException(
                            message.toString(), p_extends.getExtendsStart());
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

    private void pushOverriddenMethodUnit(AOverride p_override)
    {
        m_currentUnit = getTemplateUnit()
            .makeOverridenMethodUnit(p_override);
    }

    private FragmentUnit pushFragmentUnitImpl(String p_fragName)
    {
        m_currentUnit = new FragmentUnit(p_fragName, getCurrentUnit());
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
    private Token m_currentToken = null;
    private final TemplateUnit m_templateUnit;
    private Unit m_currentUnit;
    private final TemplateDescriber m_describer;
    private final Set m_children;
    private final Set m_defNames = new HashSet();
    private final LinkedList m_callStatements = new LinkedList();
    private final Map m_aliases = new HashMap();
    private final String m_templateDir;
    private final String m_templateIdentifier;

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
            int lastSlash = m_path.toString().lastIndexOf("/", m_path.length() - 2 );
            if (lastSlash == 0)
            {
                throw new TunnelingException
                    ("Cannot reference templates above the root",
                     p_updir.getUpdir());
            }
            m_path.delete(lastSlash + 1, m_path.length());
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
                throw new TunnelingException("Unknown alias " + alias,
                                             p_aliasedPath.getIdentifier());
            }
            else
            {
                m_path.append(prefix);
            }
        }
    }

    private class AliasAdapter extends AnalysisAdapter
    {
        private void addAlias(AAlias p_alias)
        {
            String name = p_alias.getAliasName().toString().trim();
            if (m_aliases.containsKey(name))
            {
                Token token = null;
                if (p_alias.getAliasName() instanceof ARootAliasName)
                {
                    token = ((ARootAliasName) p_alias.getAliasName())
                        .getPathsep();
                }
                else if (p_alias.getAliasName() instanceof AIdAliasName)
                {
                    token = ((AIdAliasName) p_alias.getAliasName())
                        .getIdentifier();
                }
                else // in case we forget to handle new types
                {
                    token = p_alias.getArrow();
                }
                throw new TunnelingException("Duplicate alias " + name, token);
            }
            else
            {
                m_aliases.put(name, computePath(p_alias.getPath()));
            }
        }

        public void caseAAliasComponent(AAliasComponent p_alias)
        {
            for (Iterator a = ((AAliases)p_alias.getAliases())
                     .getAlias().iterator();
                 a.hasNext(); )
            {
                addAlias((AAlias) a.next());
            }
        }
    }

    private class PreliminaryAdapter extends AnalysisAdapter
    {
        public void caseAEscapeComponent(AEscapeComponent p_escape)
        {
            if (m_defaultEscaping != null)
            {
                throw new TunnelingException
                    ("a template cannot specify multiple default escapings",
                     p_escape.getEscapeDirectiveStart());
            }
            TEscaping token = p_escape.getEscaping();
            m_defaultEscaping = token.getText();
            if (EscapingDirective.get(m_defaultEscaping) == null)
            {
                throw new AnalysisException
                    ("Unknown escaping directive '" + m_defaultEscaping + "'",
                     m_describer.getExternalIdentifier(getTemplateUnit().getName()),
                     token);
            }
        }

        public void caseAExtendsComponent(AExtendsComponent p_extends)
        {
            if(getTemplateUnit().hasParentPath())
            {
                throw new TunnelingException
                    ("a template cannot extend multiple templates",
                     p_extends.getExtendsStart());
            }
            String parentPath =
                getAbsolutePath(computePath(p_extends.getPath()));
            getTemplateUnit().setParentPath(parentPath);
            if (m_children.contains(parentPath))
            {
                throw new TunnelingException(
                    "cyclic inheritance involving " + parentPath,
                    p_extends.getExtendsStart());
            }
            else
            {
                m_children.add(parentPath);
                try
                {
                    getTemplateUnit().setParentDescription(
                        m_describer.getTemplateDescription(
                            parentPath,
                            p_extends.getExtendsStart(),
                            m_templateIdentifier,
                            m_children));
                }
                catch (JamonTemplateException e)
                {
                    throw new TunnelingException(e);
                }
                catch (IOException e)
                {
                    throw new TunnelingException(e.getMessage(),
                                                 p_extends.getExtendsStart());
                }
            }
        }

        public void caseAAbstractComponent(AAbstractComponent p_abstract)
        {
            getTemplateUnit().setIsParent();
        }

        public void caseADefComponent(ADefComponent p_def)
        {
            getTemplateUnit().
                makeDefUnit(((ADef) p_def.getDef()).getIdentifier());
        }

        public void caseAMethodComponent(AMethodComponent p_method)
        {
            getTemplateUnit().makeMethodUnit(
                ((AMethod) p_method.getMethod()).getIdentifier(),
                false);
        }

        public void caseAAbstractMethodComponent(
            AAbstractMethodComponent p_method)
        {
            getTemplateUnit().makeMethodUnit(
                ((AAbstractMethod) p_method.getAbstractMethod())
                .getIdentifier(),
                true);
        }
    }

    private class Adapter extends DepthFirstAdapter
    {
        public void caseAImport(AImport p_import)
        {
            getTemplateUnit().addImport(p_import);
        }

        public void caseAImplement(AImplement p_implement)
        {
            getTemplateUnit().addInterface(
                NodeUtils.asString(p_implement.getName()));
        }

        public void inAInheritedUse(AInheritedUse p_inheritedUse)
        {
            if (getCurrentUnit() instanceof TemplateUnit
                && ! ((TemplateUnit) getCurrentUnit()).hasParentPath())
            {
                throw new TunnelingException
                    ("xargs may not be declared without extending another template",
                     p_inheritedUse.getInheritedArgsStart());
            }
        }

        public void caseAParentArg(AParentArg p_arg)
        {
            ADefault argDefault = (ADefault) p_arg.getDefault();
            ((InheritedUnit) getCurrentUnit()).addParentArg(p_arg);
        }

        public void caseAFargStart(AFargStart p_fargStart)
        {
            pushFragmentArg(
                getCurrentUnit().addFragment(p_fargStart.getIdentifier()));
        }

        public void outAFarg(AFarg node)
        {
            popUnit();
        }

        public void caseAArglessFarg(AArglessFarg p_farg)
        {
            getCurrentUnit().addFragment(
                ((AFargTag) p_farg.getFargTag()).getIdentifier());
        }

        public void caseAArg(AArg p_arg)
        {
            getCurrentUnit().addNonFragmentArg(p_arg);
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

        public void inAAbstractMethod(AAbstractMethod p_abstractMethod)
        {
            handleBody();
            if (! getTemplateUnit().isParent())
            {
                throw new TunnelingException(
                    "Non-abstract templates cannot have abstract methods",
                    p_abstractMethod.getAbstractMethodStart());
            }
            pushMethodUnit(p_abstractMethod.getIdentifier().getText());
        }

        public void outAAbstractMethod(AAbstractMethod p_abstractMethod)
        {
            popUnit();
        }

        public void inAOverride(AOverride p_override)
        {
            handleBody();
            pushOverriddenMethodUnit(p_override);
        }

        public void outAOverride(AOverride p_override)
        {
            handleBody();
            popUnit();
        }

        public void caseABadBaseComponent(ABadBaseComponent node)
        {
            throw new TunnelingException
                ("Unknown directive \""
                 + node.getBadToken().getText()
                 + node.getRestOfToken().getText()
                 + "\"",
                 node.getBadToken());
        }

        public void inACallBaseComponent(ACallBaseComponent node)
        {
            handleBody();
        }

        public void caseACall(ACall p_call)
        {
            addStatement(makeCallStatement(p_call.getPath(),
                                           p_call.getParams(),
                                           p_call.getCallStart()));
        }

        public void caseAChildCall(AChildCall p_childCall)
        {
            TemplateUnit unit = getTemplateUnit();
            if (! unit.isParent())
            {
                throw new TunnelingException(
                    "<& *CHILD &> cannot be called from a template without an <%abstract> tag",
                    p_childCall.getCallStart());
            }
            addStatement(new ChildCallStatement(unit.getInheritanceDepth()+1));
        }

        public void inAClassComponent(AClassComponent node)
        {
            handleBody();
        }

        public void caseTClassContent(TClassContent p_content)
        {
            handleBody();
            getTemplateUnit().addClassContent(p_content);
        }

        public void caseALiteralBaseComponent(ALiteralBaseComponent node)
        {
            handleBody();
            addStatement
                (new LiteralStatement(node.getLiteralText().getText(),
                                      node.getLitstart(),
                                      m_templateIdentifier));
        }

        public void caseABodyBaseComponent(ABodyBaseComponent node)
        {
            if (m_currentToken == null)
            {
                m_currentToken = node.getText();
            }
            m_current.append(node.getText().getText());
        }

        public void caseANewlineBaseComponent(ANewlineBaseComponent node)
        {
            if (m_currentToken == null)
            {
                m_currentToken = node.getNewline();
            }
            m_current.append(node.getNewline().getText());
        }

        public void caseAPercentBaseComponent(APercentBaseComponent node)
        {
            if (m_currentToken == null)
            {
                m_currentToken = node.getPercent();
            }
            m_current.append(node.getPercent().getText());
        }

        public void inAMultiFragmentCall(AMultiFragmentCall p_call)
        {
            handleBody();
            CallStatement s = makeCallStatementWithFragments(
                p_call.getPath(),
                p_call.getParams(),
                p_call.getMultiFragmentCallInit());
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
                makeCallStatementWithFragments(p_call.getPath(),
                                               p_call.getParams(),
                                               p_call.getFragmentCallStart());
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
            addStatement(new RawStatement(p_java.getJavaStmts().getText(),
                                          p_java.getJavaStart(),
                                          m_templateIdentifier));
        }

        public void caseAPartialJline(APartialJline node)
        {
            handleBody();
            addStatement(new RawStatement(node.getExpr().getText(),
                                          node.getExpr(),
                                          m_templateIdentifier));
        }

        public void caseAJline(AJline p_jline)
        {
            handleBody();
            addStatement(new RawStatement(p_jline.getExpr().getText(),
                                          p_jline.getExpr(),
                                          m_templateIdentifier));
        }

        public void caseAEmit(AEmit p_emit)
        {
            handleBody();
            AEscape escape = (AEscape) p_emit.getEscape();
            addStatement(new WriteStatement
                         (p_emit.getEmitExpr().getText(),
                          extractEscaping(escape),
                          p_emit.getEmitStart(),
                          m_templateIdentifier));
        }

        private EscapingDirective extractEscaping(AEscape p_escape)
        {
            if (p_escape == null)
            {
                return getDefaultEscaping();
            }
            else
            {
                String directive = p_escape.getEscapeCode().getText();
                EscapingDirective escaping =
                    EscapingDirective.get(directive);
                if (escaping == null)
                {
                    throw new AnalysisException
                        ("Unknown escaping directive '" + directive + "'",
                         m_describer.getExternalIdentifier(getTemplateUnit().getName()),
                         p_escape.getEscapeCode());
                }
                else
                {
                    return escaping;
                }
            }
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
                                              m_currentToken,
                                              m_templateIdentifier));
            m_current = new StringBuffer();
            m_currentToken = null;
        }
    }

    private CallStatement makeCallStatementWithFragments(PPath p_path,
                                                         PParams p_params,
                                                         Token p_token)
    {
        CallStatement s = makeCallStatement(p_path, p_params, p_token);
        if (s instanceof FargCallStatement)
        {
            throw new TunnelingException
                ("Fragment args for fragments not implemented", p_token);
        }
        return s;
    }

    private CallStatement makeCallStatement(PPath p_path,
                                            PParams p_params,
                                            Token p_token)
    {
        String path = computePath(p_path);
        FragmentUnit fragmentUnit = getCurrentUnit().getFragmentUnitIntf(path);
        ParamValues paramValues = makeParamValues(p_params, p_token);
        if (fragmentUnit != null)
        {
            return new FargCallStatement(path,
                                         paramValues,
                                         fragmentUnit,
                                         p_token,
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
                                            p_token,
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
                                                   p_token,
                                                   m_templateIdentifier);
                }
                else
                {
                    getTemplateUnit().addCallPath(getAbsolutePath(path));
                    return new ComponentCallStatement(getAbsolutePath(path),
                                                      paramValues,
                                                      p_token,
                                                      m_templateIdentifier);
                }
            }
        }
    }

    private ParamValues makeParamValues(PParams p_params, Token p_token)
    {
        if (p_params instanceof ANamedParams)
        {
            final Map paramMap = new HashMap();
            p_params.apply(new DepthFirstAdapter()
                {
                    public void caseANamedParam(ANamedParam p_param)
                    {
                        paramMap.put(p_param.getIdentifier().getText(),
                                     p_param.getParamExpr().getText().trim());
                    }
                });
            return
                new NamedParamValues(paramMap, p_token, m_templateIdentifier);
        }
        else if (p_params instanceof AUnnamedParams)
        {
            final List params = new LinkedList();
            p_params.apply(new DepthFirstAdapter()
                {
                    public void caseAUnnamedParam(AUnnamedParam p_param)
                    {
                        params.add(p_param.getParamExpr().getText().trim());
                    }
                });
            return new UnnamedParamValues(params,
                                          p_token,
                                          m_templateIdentifier);
        }
        else
        {
            throw new IllegalStateException(
                "Unexpected subtype " + p_params.getClass() + " of PParams");
        }
    }

    private void addStatement(Statement p_statement)
    {
        getCurrentUnit().addStatement(p_statement);
    }
}
