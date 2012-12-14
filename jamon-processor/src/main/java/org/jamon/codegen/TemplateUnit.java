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

import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jamon.api.Location;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AnnotationNode;
import org.jamon.node.ClassNode;
import org.jamon.node.GenericsParamNode;
import org.jamon.node.ImportNode;
import org.jamon.node.ParentArgNode;
import org.jamon.node.StaticImportNode;
import org.jamon.util.StringUtils;

public class TemplateUnit extends AbstractUnit implements InheritedUnit {
  private InheritedArgs inheritedArgs = null;
  private TemplateDescription parentDescription = TemplateDescription.EMPTY;
  private final List<RequiredArgument> declaredRequiredArgs = new LinkedList<RequiredArgument>();
  private final List<FragmentArgument> fragmentArgs = new LinkedList<FragmentArgument>();
  private final Set<OptionalArgument> declaredOptionalArgs = new TreeSet<OptionalArgument>();
  private final Set<FragmentArgument> declaredFragmentArgs = new TreeSet<FragmentArgument>();
  private final Map<String, DefUnit> defs = new TreeMap<String, DefUnit>();
  private final Map<String, MethodUnit> methods = new TreeMap<String, MethodUnit>();
  private final List<OverriddenMethodUnit> overrides = new LinkedList<OverriddenMethodUnit>();
  private final List<ImportNode> imports = new LinkedList<ImportNode>();
  private final List<StaticImportNode> staticImports = new LinkedList<StaticImportNode>();
  private final List<String> interfaces = new LinkedList<String>();

  private String parentPath;
  private boolean isParent = false;
  private String replacedTemplatePath;
  private TemplateDescription replacedTemplateDescription;
  private boolean replaceable = false;
  private final List<ClassNode> classContent = new LinkedList<ClassNode>();
  private final Set<String> dependencies = new HashSet<String>();
  private final Set<String> callNames = new HashSet<String>();
  private final Collection<String> abstractMethodNames = new TreeSet<String>();
  private final GenericParams genericParams = new GenericParams();
  private String jamonContextType;
  private final List<AnnotationNode> annotations = new LinkedList<AnnotationNode>();
  private String encoding;

  public TemplateUnit(String path, ParserErrorsImpl errors) {
    super(path, null, errors, null);
  }

  public int getInheritanceDepth() {
    return parentDescription == null
        ? 0
        : 1 + parentDescription.getInheritanceDepth();
  }

  /**
   * public for unit testing purposes
   **/

  public void setParentDescription(TemplateDescription parent) {
    parentDescription = parent;
    // FIXME - join them later.
    fragmentArgs.addAll(parent.getFragmentInterfaces());

    inheritedArgs = new InheritedArgs(
      getParentPath(),
      parent.getRequiredArgs(),
      parent.getOptionalArgs(),
      parent.getFragmentInterfaces(),
      getErrors());

    for (AbstractArgument arg : new Concatenation<AbstractArgument>(parent.getRequiredArgs(),
        parent.getOptionalArgs(), parent.getFragmentInterfaces())) {
      addArgName(arg);
    }

    callNames.addAll(parent.getMethodUnits().keySet());
    abstractMethodNames.addAll(parent.getAbstractMethodNames());
    if (jamonContextType == null) {
      setJamonContextType(parent.getJamonContextType());
    }
  }

  @Override
  public void addParentArg(ParentArgNode arg) {
    inheritedArgs.addParentArg(arg);
  }

  @Override
  public List<FragmentArgument> getFragmentArgs() {
    return fragmentArgs;
  }

  @Override
  public void addFragmentArg(FragmentArgument arg) {
    fragmentArgs.add(arg);
    declaredFragmentArgs.add(arg);
  }

  public Collection<FragmentArgument> getDeclaredFragmentArgs() {
    return declaredFragmentArgs;
  }

  @Override
  public void addRequiredArg(RequiredArgument arg) {
    declaredRequiredArgs.add(arg);
  }

  @Override
  public void addOptionalArg(OptionalArgument arg) {
    declaredOptionalArgs.add(arg);
  }

  @Override
  public List<RequiredArgument> getSignatureRequiredArgs() {
    return new SequentialList<RequiredArgument>(
        parentDescription.getRequiredArgs(), declaredRequiredArgs);
  }

  @Override
  public Collection<OptionalArgument> getSignatureOptionalArgs() {
    return new Concatenation<OptionalArgument>(
        parentDescription.getOptionalArgs(), declaredOptionalArgs);
  }

  public String getOptionalArgDefault(OptionalArgument arg) {
    return declaredOptionalArgs.contains(arg)
        ? arg.getDefault()
        : inheritedArgs.getDefaultValue(arg);
  }

  @Override
  public Collection<AbstractArgument> getVisibleArgs() {
    return inheritedArgs == null
        ? new Concatenation<AbstractArgument>(getDeclaredRenderArgs(), getDeclaredOptionalArgs())
        : new Concatenation<AbstractArgument>(
            inheritedArgs.getVisibleArgs(), getDeclaredRenderArgs(), getDeclaredOptionalArgs());
  }

  public Collection<OptionalArgument> getDeclaredOptionalArgs() {
    return declaredOptionalArgs;
  }

  public Collection<String> getTemplateDependencies() {
    return dependencies;
  }

  private void checkCallName(String name, Location location) {
    if (!callNames.add(name)) {
      getErrors().addError("multiple defs and/or methods named " + name, location);
    }
  }

  public void makeDefUnit(String defName, Location location) {
    checkCallName(defName, location);
    defs.put(defName, new DefUnit(defName, this, getErrors(), location));
  }

  public Collection<DefUnit> getDefUnits() {
    return defs.values();
  }

  public DefUnit getDefUnit(String name) {
    return defs.get(name);
  }

  public void makeMethodUnit(String methodName, Location location, boolean isAbstract) {
    checkCallName(methodName, location);
    methods.put(methodName, new DeclaredMethodUnit(methodName, this, getErrors(), location,
        isAbstract));
    if (isAbstract) {
      abstractMethodNames.add(methodName);
    }
  }

  public OverriddenMethodUnit makeOverridenMethodUnit(String name, Location location) {
    DeclaredMethodUnit methodUnit = (DeclaredMethodUnit) parentDescription.getMethodUnits().get(
      name);
    if (methodUnit == null) {
      getErrors().addError("There is no such method " + name + " to override", location);
      // Provide a dummy parent, to allow us to catch errors inside the
      // override
      methodUnit = new DeclaredMethodUnit(name, this, getErrors(), location);
    }

    abstractMethodNames.remove(name);
    OverriddenMethodUnit override =
      new OverriddenMethodUnit(methodUnit, this, getErrors(), location);
    overrides.add(override);
    return override;
  }

  public MethodUnit getMethodUnit(String name) {
    return (methods.containsKey(name)
        ? methods.get(name)
        : parentDescription.getMethodUnits().get(name));
  }

  public Collection<MethodUnit> getSignatureMethodUnits() {
    return new Concatenation<MethodUnit>(
        getDeclaredMethodUnits(), parentDescription.getMethodUnits().values());
  }

  public Collection<MethodUnit> getDeclaredMethodUnits() {
    return methods.values();
  }

  public Collection<MethodUnit> getImplementedMethodUnits() {
    return new Concatenation<MethodUnit>(getDeclaredMethodUnits(), overrides);
  }

  public Collection<String> getAbstractMethodNames() {
    return abstractMethodNames;
  }

  private Iterable<ImportNode> getImports() {
    return imports;
  }

  public void addStaticImport(StaticImportNode node) {
    staticImports.add(node);
  }

  private Iterable<StaticImportNode> getStaticImports() {
    return staticImports;
  }

  public void addImport(ImportNode node) {
    imports.add(node);
  }

  public void addInterface(String interfase) {
    interfaces.add(interfase);
  }

  public void setParentPath(String parentPath) {
    this.parentPath = parentPath;
    dependencies.add(parentPath);
  }

  public String getParentPath() {
    return parentPath;
  }

  public boolean hasParentPath() {
    return parentPath != null;
  }

  /**
   * Set the path of the template which this template replaces, along with the description of the
   * replaced template.
   *
   * @param replacedTemplatePath the path of the template which this template replaced
   * @param templateDescription the description of the replaced template
   */
  public void setReplacedTemplatePathAndDescription(
    String replacedTemplatePath, TemplateDescription templateDescription) {
    this.replacedTemplatePath = replacedTemplatePath;
    this.replacedTemplateDescription = templateDescription;
    dependencies.add(replacedTemplatePath);
  }

  /**
   * Get the path of the template which this template replaces, or null if it is not replacing a
   * template.
   *
   * @return the path of the template which this template replaces, or null if it is not replacing a
   *         template.
   */
  public String getReplacedTemplatePath() {
    return replacedTemplatePath;
  }

  /**
   * Get the description of the template this template is replacing, or null if it is not replacing
   * a template
   *
   * @return the description of the template this template is replacing, or null if it is not
   *         replacing a template
   */
  public TemplateDescription getReplacedTemplateDescription() {
    return replacedTemplateDescription;
  }

  /**
   * Whether this template replaces another template. This is the case if the template has a {@code
   * <%replacesTemplate ...>} tag.
   *
   * @return {@code true} if this template replaces another template.
   */
  public boolean isReplacing() {
    return replacedTemplatePath != null;
  }

  /**
   * Set whether this template can be replaced by another one.
   *
   * @param replaceable whether this template can be replaced by another one.
   */
  public void setReplaceable(boolean replaceable) {
    this.replaceable = replaceable;
  }

  /**
   * Get whether this template can be replaced by another one.
   *
   * @return whether this template can be replaced by another one.
   */
  public boolean isReplaceable() {
    return replaceable;
  }

  public String getProxyParentClass() {
    return hasParentPath()
        ? PathUtils.getFullyQualifiedIntfClassName(getParentPath())
        : ClassNames.TEMPLATE;
  }

  public boolean isParent() {
    return isParent;
  }

  public void setIsParent() {
    isParent = true;
  }

  public void printClassContent(CodeWriter writer) {
    for (ClassNode node : classContent) {
      writer.printLocation(node.getLocation());
      writer.println(node.getContent());
    }
  }

  public void addClassContent(ClassNode node) {
    classContent.add(node);
  }

  public void addCallPath(String callPath) {
    dependencies.add(callPath);
  }

  public Collection<RequiredArgument> getParentRenderArgs() {
    return new Concatenation<RequiredArgument>(
        parentDescription.getRequiredArgs(), parentDescription.getFragmentInterfaces());
  }

  public void printImports(CodeWriter writer) {
    for (ImportNode node : getImports()) {
      writer.printLocation(node.getLocation());
      writer.println("import " + node.getName() + ";");
    }
    for (StaticImportNode node : getStaticImports()) {
      writer.printLocation(node.getLocation());
      writer.println("import static " + node.getName() + ";");
    }
    writer.println();
  }

  public void printParentRenderArgs(CodeWriter writer) {
    printArgs(writer, getParentRenderArgs());
  }

  public void printParentRenderArgsDecl(CodeWriter writer) {
    printArgsDecl(writer, getParentRenderArgs());
  }

  public Collection<? extends RequiredArgument> getDeclaredRenderArgs() {
    return new Concatenation<RequiredArgument>(declaredRequiredArgs, declaredFragmentArgs);
  }

  public Collection<AbstractArgument> getDeclaredArgs() {
    return new Concatenation<AbstractArgument>(getDeclaredRenderArgs(), declaredOptionalArgs);
  }

  public void printDeclaredRenderArgs(CodeWriter writer) {
    printArgs(writer, getDeclaredRenderArgs());
  }

  public void printDeclaredRenderArgsDecl(CodeWriter writer) {
    printArgsDecl(writer, getDeclaredRenderArgs());
  }

  public void printInterfaces(CodeWriter writer) {
    if (interfaces.size() > 0) {
      writer.print("  implements ");
      writer.openList("", false);
      for (String intrface : interfaces) {
        writer.printListElement(intrface);
      }
      writer.closeList("");
    }
  }

  @Override
  protected void generateInterfaceSummary(StringBuilder buf) {
    super.generateInterfaceSummary(buf);
    buf.append("GenericParams:");
    buf.append(getGenericParams().generateGenericsDeclaration());
    buf.append("\n");
    buf.append("replaceable:").append(isReplaceable()).append("\n");
    if (parentDescription != null) {
      buf.append("Parent sig: ").append(parentDescription.getSignature()).append("\n");
    }
    for (FragmentArgument arg : getFragmentArgs()) {
      buf.append("Fragment: ").append(arg.getName()).append("\n");
      arg.getFragmentUnit().generateInterfaceSummary(buf);
    }
  }

  /**
   * Get the signature hash for this template. The signature is a hash which will change in the
   * event that the template's API has changed.
   *
   * @return the signature hash for this template
   */
  public String getSignature() {
    try {
      StringBuilder buf = new StringBuilder();
      generateInterfaceSummary(buf);
      return StringUtils.byteArrayToHexString(
        MessageDigest.getInstance("MD5").digest(buf.toString().getBytes("UTF-8")));
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to generate signature", e);
    }
  }

  public GenericParams getGenericParams() {
    return genericParams;
  }

  public void setJamonContextType(String jamonContextType) {
    this.jamonContextType = jamonContextType;
  }

  public String getJamonContextType() {
    return jamonContextType;
  }

  public boolean isOriginatingJamonContext() {
    return jamonContextType != null
      && (!hasParentPath() || parentDescription.getJamonContextType() == null);
  }

  public void addGenericsParamNode(GenericsParamNode node) {
    genericParams.addParam(node);
  }

  public void addAnnotationNode(AnnotationNode node) {
    annotations.add(node);
  }

  public Iterable<AnnotationNode> getAnnotations() {
    return annotations;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getEncoding() {
    return encoding;
  }
}
