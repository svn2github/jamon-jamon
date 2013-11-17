/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jamon.util.AnnotationReflector;
import org.jamon.annotations.Argument;
import org.jamon.annotations.Fragment;
import org.jamon.annotations.Method;
import org.jamon.annotations.Template;

public class TemplateDescription {
  private final List<RequiredArgument> requiredArgs;
  private final Set<OptionalArgument> optionalArgs;
  private final String signature;
  private final List<FragmentArgument> fragmentInterfaces;
  private final Map<String, MethodUnit> methodUnits;
  private final int inheritanceDepth;
  private final Collection<String> abstractMethodNames;
  private final int genericParamsCount;
  private final String jamonContextType;
  private final boolean replaceable;
  public final static TemplateDescription EMPTY = new TemplateDescription();

  private TemplateDescription() {
    requiredArgs = Collections.emptyList();
    optionalArgs = Collections.emptySet();
    signature = null;
    fragmentInterfaces = Collections.emptyList();
    methodUnits = Collections.emptyMap();
    inheritanceDepth = -1;
    abstractMethodNames = Collections.emptyList();
    genericParamsCount = 0;
    jamonContextType = null;
    replaceable = false;
  }

  public TemplateDescription(TemplateUnit templateUnit) {
    requiredArgs = new LinkedList<RequiredArgument>();
    requiredArgs.addAll(templateUnit.getSignatureRequiredArgs());
    optionalArgs = new TreeSet<OptionalArgument>();
    optionalArgs.addAll(templateUnit.getSignatureOptionalArgs());
    for (OptionalArgument arg : optionalArgs) {
      arg.setDefault(null);
    }
    signature = templateUnit.getSignature();
    fragmentInterfaces = templateUnit.getFragmentArgs();
    methodUnits = new TreeMap<String, MethodUnit>();
    for (MethodUnit methodUnit : templateUnit.getSignatureMethodUnits()) {
      methodUnits.put(methodUnit.getName(), methodUnit);
    }
    inheritanceDepth = templateUnit.getInheritanceDepth();
    abstractMethodNames = templateUnit.getAbstractMethodNames();
    genericParamsCount = templateUnit.getGenericParams().getCount();
    jamonContextType = templateUnit.getJamonContextType();
    replaceable = templateUnit.isReplaceable();
  }

  public TemplateDescription(Class<?> proxy) throws NoSuchFieldException {
    Template templateAnnotation = new AnnotationReflector(proxy).getAnnotation(Template.class);
    if (templateAnnotation == null) {
      // FIXME - throw something more sensical here
      throw new NoSuchFieldException("class " + proxy.getName() + " lacks a template annotation");
    }

    requiredArgs = getRequiredArgs(templateAnnotation.requiredArguments());
    optionalArgs = getOptionalArgs(templateAnnotation.optionalArguments());

    fragmentInterfaces = getFragmentArguments(templateAnnotation.fragmentArguments(),
      new TemplateUnit(PathUtils.getPathForProxyClass(proxy), null));
    methodUnits = new TreeMap<String, MethodUnit>();
    for (Method methodAnnotation : templateAnnotation.methods()) {
      DeclaredMethodUnit method = new DeclaredMethodUnit(methodAnnotation.name(), null, null, null);
      for (Argument argument : methodAnnotation.requiredArguments()) {
        method.addRequiredArg(makeRequiredArg(argument));
      }
      for (Argument argument : methodAnnotation.optionalArguments()) {
        method.addOptionalArg(makeOptionalArg(argument));
      }
      for (Fragment fragment : methodAnnotation.fragmentArguments()) {
        method.addFragmentArg(makeFragmentArg(method, fragment));
      }
      methodUnits.put(method.getName(), method);
    }
    abstractMethodNames = Arrays.asList(templateAnnotation.abstractMethodNames());
    signature = templateAnnotation.signature();
    inheritanceDepth = templateAnnotation.inheritanceDepth();
    genericParamsCount = templateAnnotation.genericsCount();
    jamonContextType = nullToEmptyString(templateAnnotation.jamonContextType());
    replaceable = templateAnnotation.replaceable();
  }

  private String nullToEmptyString(String jamonContextType) {
    return jamonContextType.equals("")
        ? null
        : jamonContextType;
  }

  private static List<RequiredArgument> getRequiredArgs(Argument[] arguments) {
    List<RequiredArgument> args = new ArrayList<RequiredArgument>(arguments.length);
    for (Argument argument : arguments) {
      args.add(makeRequiredArg(argument));
    }
    return args;
  }

  private static Set<OptionalArgument> getOptionalArgs(Argument[] arguments) {
    Set<OptionalArgument> args = new TreeSet<OptionalArgument>();
    for (Argument argument : arguments) {
      args.add(makeOptionalArg(argument));
    }
    return args;
  }

  private static RequiredArgument makeRequiredArg(Argument argument) {
    return new RequiredArgument(argument.name(), argument.type(), null);
  }

  private static OptionalArgument makeOptionalArg(Argument argument) {
    return new OptionalArgument(argument.name(), argument.type(), null);
  }

  private static List<FragmentArgument> getFragmentArguments(
    Fragment[] fragments, Unit parentUnit) {
    List<FragmentArgument> fragmentArguments = new ArrayList<FragmentArgument>(fragments.length);
    for (Fragment fragment : fragments) {
      fragmentArguments.add(makeFragmentArg(parentUnit, fragment));
    }
    return fragmentArguments;
  }

  private static FragmentArgument makeFragmentArg(Unit parentUnit, Fragment fragment) {
    FragmentUnit fragmentUnit = new FragmentUnit(fragment.name(), parentUnit,
        new GenericParams(), null, null);
    for (Argument argument : fragment.requiredArguments()) {
      fragmentUnit.addRequiredArg(makeRequiredArg(argument));
    }
    FragmentArgument fragmentArgument = new FragmentArgument(fragmentUnit, null);
    return fragmentArgument;
  }

  public List<RequiredArgument> getRequiredArgs() {
    return requiredArgs;
  }

  public Set<OptionalArgument> getOptionalArgs() {
    return optionalArgs;
  }

  /**
   * Get the signature hash for the described template. The signature is a hash which will change in
   * the event that the template's API has changed.
   *
   * @return the signature hash for this template
   */
  public String getSignature() {
    return signature;
  }

  public List<FragmentArgument> getFragmentInterfaces() {
    return fragmentInterfaces;
  }

  public Map<String, MethodUnit> getMethodUnits() {
    return methodUnits;
  }

  public Collection<String> getAbstractMethodNames() {
    return abstractMethodNames;
  }

  public int getInheritanceDepth() {
    return inheritanceDepth;
  }

  public int getGenericParamsCount() {
    return genericParamsCount;
  }

  public String getJamonContextType() {
    return jamonContextType;
  }

  public boolean isReplaceable() {
    return replaceable;
  }
}
