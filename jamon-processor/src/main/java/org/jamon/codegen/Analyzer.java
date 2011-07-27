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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.emit.EmitMode;
import org.jamon.node.*;
import org.jamon.util.StringUtils;

public class Analyzer {
  static final String ABSTRACT_REPLACING_TEMPLATE_ERROR = "An abstract template cannot replace another template";

  static final String ABSTRACT_REPLACEABLE_TEMPLATE_ERROR = "Abstract templates are not replaceable";

  static final String REPLACING_NON_REPLACEABLE_TEMPLATE_ERROR = "Replaced template is not marked as <%replaceable>";

  static final String XARGS_DECLARED_WITHOUT_PARENT_ERROR = "xargs may not be declared without extending another template";

  public Analyzer(String templatePath, TemplateDescriber describer, Set<String> children)
      throws IOException {
    templateUnit = new TemplateUnit(templatePath, errors);
    templateDir = templatePath.substring(0, 1 + templatePath.lastIndexOf('/'));
    currentStatementBlock = templateUnit;
    this.describer = describer;
    this.children = children;
    emitMode = describer.getEmitMode(templatePath);
    templateIdentifier = describer.getExternalIdentifier(templatePath);
    templateUnit.setJamonContextType(describer.getJamonContextType(templatePath));
    aliases.putAll(describer.getAliases(templatePath));
  }

  public Analyzer(String templatePath, TemplateDescriber describer) throws IOException {
    this(templatePath, describer, new HashSet<String>());
  }

  public TemplateUnit analyze() throws IOException {
    TopNode top = describer.parseTemplate(templateUnit.getName());
    templateUnit.setEncoding(top.getEncoding());
    preAnalyze(top);
    mainAnalyze(top);
    checkForConcreteness(top);
    checkTemplateReplacement(top);
    if (errors.hasErrors()) {
      throw errors;
    }
    return templateUnit;
  }

  private void addError(String message, Location location) {
    errors.addError(new ParserErrorImpl(location, message));
  }

  private void preAnalyze(TopNode top) throws IOException {
    topLevelAnalyze(top, new AliasAdapter());
    topLevelAnalyze(top, new PreliminaryAdapter());
    if (defaultEscaping == null) {
      defaultEscaping = describer.getEscaping(templateUnit.getName());
    }
    if (defaultEscaping == null) {
      defaultEscaping = EscapingDirective.get(EscapingDirective.DEFAULT_ESCAPE_CODE);
    }
  }

  private void topLevelAnalyze(TopNode top, AnalysisAdapter adapter) {
    for (AbstractNode node : top.getSubNodes()) {
      node.apply(adapter);
    }

  }

  private void mainAnalyze(TopNode top) {
    top.apply(new Adapter());
  }

  private void checkForConcreteness(TopNode top) {
    if (!getTemplateUnit().isParent() && !getTemplateUnit().getAbstractMethodNames().isEmpty()) {
      topLevelAnalyze(top, new AnalysisAdapter() {
        @Override
        public void caseExtendsNode(ExtendsNode extendsNode) {
          StringBuilder message = new StringBuilder("The abstract method(s) ");
          StringUtils.commaJoin(message, getTemplateUnit().getAbstractMethodNames());
          message.append(" have no concrete implementation");
          addError(message.toString(), extendsNode.getLocation());
        }
      });
    }
  }

  /**
   * If this template is a replacement template (via the <%replaces> tag), verify that:
   * <ul>
   * <li>The replacing template is concrete</li>
   * <li>All required arguments have matching required arguments in the replaced template</li>
   * <li>All optional argument have matching required or optional arguments in the replaced template
   * </li>
   * <li>All fragment arguments have matching fragment arguments in the replaced template</li> </li>
   *
   * @param top the top node of the syntax tree.
   */
  private void checkTemplateReplacement(TopNode top) {
    topLevelAnalyze(top, new AnalysisAdapter() {
      @Override
      public void caseReplaceableNode(ReplaceableNode replaceable) {
        if (getTemplateUnit().isParent()) {
          addError(ABSTRACT_REPLACEABLE_TEMPLATE_ERROR, replaceable.getLocation());
        }
        else {
          getTemplateUnit().setReplaceable(true);
        }
      }

      @Override
      public void caseReplacesNode(ReplacesNode replaces) {
        if (getTemplateUnit().isParent()) {
          addError(ABSTRACT_REPLACING_TEMPLATE_ERROR, replaces.getLocation());
        }
        else {
          String replacedTemplatePath = getAbsolutePath(computePath(replaces.getPath()));
          TemplateDescription replacedTemplateDescription = getTemplateDescription(
            replacedTemplatePath, replaces.getLocation());
          if (replacedTemplateDescription != null) {
            if (!replacedTemplateDescription.isReplaceable()) {
              addError(REPLACING_NON_REPLACEABLE_TEMPLATE_ERROR, replaces.getLocation());
            }
            getTemplateUnit().setReplacedTemplatePathAndDescription(replacedTemplatePath,
              replacedTemplateDescription);
            verifyRequiredArgsComeFromReplacedTemplate(replaces.getLocation(),
              replacedTemplateDescription);
            verifyFragmentArgsComeFromReplacedTemplate(replaces.getLocation(),
              replacedTemplateDescription);
            verifyOptionalArgsComeFromReplacedTemplate(replaces.getLocation(),
              replacedTemplateDescription);
          }
        }
      }
    });
  }

  private void verifyRequiredArgsComeFromReplacedTemplate(Location location,
    TemplateDescription replacedTemplateDescription) {
    verifyArgsComeFromReplacedTemplate(location, replacedTemplateDescription.getRequiredArgs()
        .iterator(), getTemplateUnit().getSignatureRequiredArgs(), "required");
  }

  private void verifyFragmentArgsComeFromReplacedTemplate(Location location,
    TemplateDescription replacedTemplateDescription) {
    verifyArgsComeFromReplacedTemplate(location, replacedTemplateDescription
        .getFragmentInterfaces().iterator(), getTemplateUnit().getFragmentArgs(), "fragment");
  }

  private void verifyOptionalArgsComeFromReplacedTemplate(Location location,
    TemplateDescription replacedTemplateDescription) {
    verifyArgsComeFromReplacedTemplate(location, new SequentialIterator<AbstractArgument>(
        replacedTemplateDescription.getRequiredArgs().iterator(), replacedTemplateDescription
            .getOptionalArgs().iterator()), getTemplateUnit().getSignatureOptionalArgs(),
      "required or optional");
  }

  private <T extends AbstractArgument> void verifyArgsComeFromReplacedTemplate(Location location,
    Iterator<T> replacedTemplateArgs, Collection<? extends T> templateArgs, String argumentKind) {
    Set<String> replacedTemplateArgNames = new HashSet<String>();
    while (replacedTemplateArgs.hasNext()) {
      replacedTemplateArgNames.add(replacedTemplateArgs.next().getName());
    }

    for (T arg : templateArgs) {
      if (!replacedTemplateArgNames.contains(arg.getName())) {
        addError("Replaced template contains no " + argumentKind + " argument named "
          + arg.getName(), location);
      }
    }
  }

  private void pushDefUnit(String defName) {
    currentStatementBlock = getTemplateUnit().getDefUnit(defName);
  }

  private void pushMethodUnit(String methodName) {
    currentStatementBlock = getTemplateUnit().getMethodUnit(methodName);
  }

  private void pushOverriddenMethodUnit(OverrideNode node) {
    currentStatementBlock = getTemplateUnit().makeOverridenMethodUnit(node.getName(),
      node.getLocation());
  }

  private void pushFlowControlBlock(Location location, String header) {
    FlowControlBlock flowControlBlock = new FlowControlBlock(currentStatementBlock, header,
        location);
    addStatement(flowControlBlock);
    currentStatementBlock = flowControlBlock;
  }

  private FragmentUnit pushFragmentUnitImpl(String fragName, Location location) {
    currentStatementBlock = new FragmentUnit(fragName, getCurrentStatementBlock(),
        getTemplateUnit().getGenericParams(), errors, location);
    return (FragmentUnit) currentStatementBlock;
  }

  private void pushFragmentArg(FragmentUnit frag) {
    currentStatementBlock = frag;
  }

  private void popStatementBlock() {
    currentStatementBlock = currentStatementBlock.getParent();
  }

  private void pushCallStatement(CallStatement callStatement) {
    callStatements.add(callStatement);
  }

  private void popCallStatement() {
    callStatements.removeLast();
  }

  private CallStatement getCurrentCallStatement() {
    return callStatements.getLast();
  }

  private TemplateUnit getTemplateUnit() {
    return templateUnit;
  }

  private StatementBlock getCurrentStatementBlock() {
    return currentStatementBlock;
  }

  private final TemplateUnit templateUnit;

  private StatementBlock currentStatementBlock;

  private final TemplateDescriber describer;

  private final Set<String> children;

  private final LinkedList<CallStatement> callStatements = new LinkedList<CallStatement>();

  private final Map<String, String> aliases = new HashMap<String, String>();

  private final String templateDir;

  private final String templateIdentifier;

  private final EmitMode emitMode;

  private ParserErrorsImpl errors = new ParserErrorsImpl();

  private String getAbsolutePath(String path) {
    return path.charAt(0) == '/'
        ? path
        : templateDir + path;
  }

  private String computePath(AbstractPathNode path) {
    PathAdapter adapter = new PathAdapter(templateDir, aliases, errors);
    path.apply(adapter);
    return adapter.getPath();
  }

  private class AliasAdapter extends AnalysisAdapter {
    @Override
    public void caseAliasesNode(AliasesNode node) {
      for (AliasDefNode defNode : node.getAliass()) {
        handleAlias(defNode);
      }
    }

    private void handleAlias(AliasDefNode node) {
      if (aliases.containsKey(node.getName())) {
        addError("Duplicate alias for " + node.getName(), node.getLocation());
      }
      else {
        aliases.put(node.getName(), computePath(node.getPath()));
      }
    }
  }

  private class PreliminaryAdapter extends AnalysisAdapter {
    @Override
    public void caseEscapeDirectiveNode(EscapeDirectiveNode escape) {
      if (defaultEscaping != null) {
        addError("a template cannot specify multiple default escapings", escape.getLocation());
      }
      defaultEscaping = EscapingDirective.get(escape.getEscapeCode());
      if (defaultEscaping == null) {
        addError("Unknown escaping directive '" + escape.getEscapeCode() + "'", escape
            .getLocation());
      }
    }

    @Override
    public void caseExtendsNode(ExtendsNode extendsNode) {
      if (getTemplateUnit().hasParentPath()) {
        addError("a template cannot extend multiple templates", extendsNode.getLocation());
      }
      String parentPath = getAbsolutePath(computePath(extendsNode.getPath()));
      TemplateDescription parentDescription = getTemplateDescription(parentPath, extendsNode
          .getLocation());
      if (parentDescription != null) {
        getTemplateUnit().setParentPath(parentPath);
        getTemplateUnit().setParentDescription(parentDescription);
      }
    }

    @Override
    public void caseAnnotationNode(AnnotationNode node) {
      templateUnit.addAnnotationNode(node);
    }

    @Override
    public void caseParentMarkerNode(ParentMarkerNode node) {
      getTemplateUnit().setIsParent();
    }

    @Override
    public void caseDefNode(DefNode node) {
      getTemplateUnit().makeDefUnit(node.getName(), node.getLocation());
    }

    @Override
    public void caseMethodNode(MethodNode node) {
      getTemplateUnit().makeMethodUnit(node.getName(), node.getLocation(), false);
    }

    @Override
    public void caseAbsMethodNode(AbsMethodNode node) {
      getTemplateUnit().makeMethodUnit(node.getName(), node.getLocation(), true);
    }
  }

  private class Adapter extends DepthFirstAnalysisAdapter {
    @Override
    public void caseImportNode(ImportNode importNode) {
      getTemplateUnit().addImport(importNode);
    }

    @Override
    public void caseStaticImportNode(StaticImportNode staticImportNode) {
      getTemplateUnit().addStaticImport(staticImportNode);
    }

    @Override
    public void caseImplementNode(ImplementNode node) {
      getTemplateUnit().addInterface(node.getName());
    }

    @Override
    public void caseParentArgsNode(ParentArgsNode node) {
      if (getCurrentStatementBlock() instanceof TemplateUnit
        && !((TemplateUnit) getCurrentStatementBlock()).hasParentPath()) {
        addError(XARGS_DECLARED_WITHOUT_PARENT_ERROR, node.getLocation());
      }
      else {
        super.caseParentArgsNode(node);
      }
    }

    @Override
    public void caseParentArgNode(ParentArgNode node) {
      ((InheritedUnit) getCurrentStatementBlock()).addParentArg(node);
    }

    @Override
    public void caseParentArgWithDefaultNode(ParentArgWithDefaultNode node) {
      ((InheritedUnit) getCurrentStatementBlock()).addParentArg(node);
    }

    @Override
    public void inFragmentArgsNode(FragmentArgsNode node) {
      pushFragmentArg(getCurrentStatementBlock().addFragment(node,
        getTemplateUnit().getGenericParams()));
    }

    @Override
    public void outFragmentArgsNode(FragmentArgsNode node) {
      popStatementBlock();
    }

    @Override
    public void caseArgNode(ArgNode node) {
      getCurrentStatementBlock().addRequiredArg(node);
    }

    @Override
    public void caseOptionalArgNode(OptionalArgNode node) {
      getCurrentStatementBlock().addOptionalArg(node);
    }

    @Override
    public void inDefNode(DefNode node) {
      pushDefUnit(node.getName());
    }

    @Override
    public void outDefNode(DefNode def) {
      popStatementBlock();
    }

    @Override
    public void inMethodNode(MethodNode node) {
      pushMethodUnit(node.getName());
    }

    @Override
    public void outMethodNode(MethodNode node) {
      popStatementBlock();
    }

    @Override
    public void inAbsMethodNode(AbsMethodNode node) {
      if (!getTemplateUnit().isParent()) {
        addError("Non-abstract templates cannot have abstract methods", node.getLocation());
      }
      pushMethodUnit(node.getName());
    }

    @Override
    public void outAbsMethodNode(AbsMethodNode node) {
      popStatementBlock();
    }

    @Override
    public void inOverrideNode(OverrideNode node) {
      pushOverriddenMethodUnit(node);
    }

    @Override
    public void outOverrideNode(OverrideNode node) {
      popStatementBlock();
    }

    @Override
    public void caseGenericsParamNode(GenericsParamNode node) {
      if (templateUnit.isParent()) {
        addError("<%generics> tag not allowed in abstract templates", node.getLocation());
      }
      templateUnit.addGenericsParamNode(node);
    }

    @Override
    public void caseSimpleCallNode(SimpleCallNode node) {
      addStatement(makeCallStatement(node));
    }

    @Override
    public void caseChildCallNode(ChildCallNode node) {
      TemplateUnit unit = getTemplateUnit();
      if (!unit.isParent()) {
        addError("<& *CHILD &> cannot be called from a template without an <%abstract> tag", node
            .getLocation());
      }
      addStatement(new ChildCallStatement(unit.getInheritanceDepth() + 1));
    }

    @Override
    public void caseClassNode(ClassNode node) {
      getTemplateUnit().addClassContent(node);
    }

    @Override
    public void caseTextNode(TextNode node) {
      addStatement(new LiteralStatement(node.getText(), node.getLocation(),
          templateIdentifier));
    }

    @Override
    public void caseLiteralNode(LiteralNode node) {
      addStatement(new LiteralStatement(node.getText(), node.getLocation(),
          templateIdentifier));
    }

    @Override
    public void inMultiFragmentCallNode(MultiFragmentCallNode node) {
      CallStatement s = makeCallStatement(node);
      addStatement(s);
      pushCallStatement(s);
    }

    @Override
    public void outMultiFragmentCallNode(MultiFragmentCallNode call) {
      popCallStatement();
    }

    @Override
    public void inNamedFragmentNode(NamedFragmentNode node) {
      getCurrentCallStatement().addFragmentImpl(
        pushFragmentUnitImpl(node.getName(), node.getLocation()), errors);
    }

    @Override
    public void outNamedFragmentNode(NamedFragmentNode node) {
      popStatementBlock();
    }

    @Override
    public void inFragmentCallNode(FragmentCallNode node) {
      CallStatement s = makeCallStatement(node);
      addStatement(s);
      s.addFragmentImpl(pushFragmentUnitImpl(null, node.getLocation()), errors);
    }

    @Override
    public void outFragmentCallNode(FragmentCallNode node) {
      popStatementBlock();
    }

    @Override
    public void inWhileNode(WhileNode node) {
      pushFlowControlBlock(node.getLocation(), "while (" + node.getCondition() + ")");
    }

    @Override
    public void outWhileNode(WhileNode node) {
      popStatementBlock();
    }

    @Override
    public void inForNode(ForNode node) {
      pushFlowControlBlock(node.getLocation(), "for (" + node.getLoopSpecifier() + ")");
    }

    @Override
    public void outForNode(ForNode node) {
      popStatementBlock();
    }

    @Override
    public void inIfNode(IfNode node) {
      pushFlowControlBlock(node.getLocation(), "if (" + node.getCondition() + ")");
    }

    @Override
    public void outIfNode(IfNode node) {
      popStatementBlock();
    }

    @Override
    public void inElseIfNode(ElseIfNode node) {
      pushFlowControlBlock(node.getLocation(), "else if (" + node.getCondition() + ")");
    }

    @Override
    public void outElseIfNode(ElseIfNode node) {
      popStatementBlock();
    }

    @Override
    public void inElseNode(ElseNode node) {
      pushFlowControlBlock(node.getLocation(), "else");
    }

    @Override
    public void outElseNode(ElseNode node) {
      popStatementBlock();
    }

    @Override
    public void outTopNode(TopNode node) {}

    @Override
    public void caseJavaNode(JavaNode node) {
      addStatement(new RawStatement(node.getJava(), node.getLocation(), templateIdentifier));
    }

    private class EmitAdapter extends AnalysisAdapter {
      EscapingDirective escapeCode = null;

      @Override
      public void caseEscapeNode(EscapeNode node) {
        escapeCode = EscapingDirective.get(node.getEscapeCode());
        if (escapeCode == null) {
          addError("Unknown escaping directive '" + node.getEscapeCode() + "'", node
              .getLocation());
        }
      }

      @Override
      public void caseDefaultEscapeNode(DefaultEscapeNode node) {
        escapeCode = getDefaultEscaping();
      }

      public EscapingDirective getEscape(AbstractEscapeNode node) {
        node.apply(this);
        return escapeCode;
      }
    }

    @Override
    public void caseEmitNode(EmitNode node) {
      addStatement(new WriteStatement(node.getEmitExpression(), new EmitAdapter()
          .getEscape(node.getEscaping()), node.getLocation(), templateIdentifier, emitMode));
    }
  }

  private EscapingDirective defaultEscaping;

  private EscapingDirective getDefaultEscaping() {
    return defaultEscaping;
  }

  private CallStatement makeCallStatement(AbstractComponentCallNode node) {
    String path = computePath(node.getCallPath());
    ParamValues paramValues = makeParamValues(node.getParams());
    FragmentUnit fragmentUnit = getCurrentStatementBlock().getFragmentUnitIntf(path);
    List<GenericCallParam> genericParams = node.getGenericParams();
    if (fragmentUnit != null) {
      if (!genericParams.isEmpty()) {
        addError("Fragment" + " calls may not have generic params", node.getLocation());
      }
      return new FargCallStatement(path, paramValues, fragmentUnit, node.getLocation(),
          templateIdentifier);
    }
    else {
      DefUnit defUnit = getTemplateUnit().getDefUnit(path);
      if (defUnit != null) {
        if (!genericParams.isEmpty()) {
          addError("def " + defUnit.getName() + " is being called with generic parameters", node
              .getLocation());
        }
        return new DefCallStatement(path, paramValues, defUnit, node.getLocation(),
            templateIdentifier);
      }
      else {
        MethodUnit methodUnit = getTemplateUnit().getMethodUnit(path);
        if (methodUnit != null) {
          if (!genericParams.isEmpty()) {
            addError("method" + methodUnit.getName() + " is being called with generic parameters",
              node.getLocation());
          }
          return new MethodCallStatement(path, paramValues, methodUnit, node.getLocation(),
              templateIdentifier);
        }
        else {
          getTemplateUnit().addCallPath(getAbsolutePath(path));
          return new ComponentCallStatement(getAbsolutePath(path), paramValues, node
              .getLocation(), templateIdentifier, genericParams, getTemplateUnit()
              .getJamonContextType());
        }
      }
    }
  }

  private static class ParamsAdapter extends DepthFirstAnalysisAdapter {
    public ParamValues getParamValues(AbstractParamsNode node) {
      node.apply(this);
      if (paramsList != null) {
        return new UnnamedParamValues(paramsList, node.getLocation());
      }
      else {
        return new NamedParamValues(paramsMap, node.getLocation());
      }
    }

    @Override
    public void inNamedParamsNode(NamedParamsNode node) {
      paramsMap = new HashMap<String, String>();
    }

    @Override
    public void inUnnamedParamsNode(UnnamedParamsNode node) {
      paramsList = new LinkedList<String>();
    }

    @Override
    public void caseNamedParamNode(NamedParamNode node) {
      paramsMap.put(node.getName().getName(), node.getValue().getValue());
    }

    @Override
    public void caseParamValueNode(ParamValueNode node) {
      paramsList.add(node.getValue());
    }

    private Map<String, String> paramsMap = null;

    private List<String> paramsList = null;
  }

  private ParamValues makeParamValues(AbstractParamsNode params) {
    return new ParamsAdapter().getParamValues(params);
  }

  private void addStatement(Statement statement) {
    getCurrentStatementBlock().addStatement(statement);
  }

  private TemplateDescription getTemplateDescription(String path, Location location) {
    if (children.contains(path)) {
      addError("cyclic inheritance or replacement involving " + path, location);
      return null;
    }
    else {
      children.add(path);
      try {
        return describer.getTemplateDescription(path, location, children);
      }
      catch (ParserErrorImpl e) {
        errors.addError(e);
      }
      catch (ParserErrorsImpl e) {
        errors.addErrors(e);
      }
      catch (IOException e) {
        addError(e.getMessage(), location);
      }
      return null;
    }
  }
}
