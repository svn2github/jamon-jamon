/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jamon.AbstractTemplateProxy.ReplacementConstructor;
import org.jamon.annotations.Replaceable;
import org.jamon.annotations.Replaces;

/**
 * A {@code TemplateReplacer} which ignores any {@code jamonContext} and replaces templates based on
 * a fixed list of template passed in at startup. While reflection is used when this class is
 * constructed, after that it is reflection-free.
 */
public class FixedTemplateReplacer extends AbstractTemplateReplacer {
  private final Map<Class<? extends AbstractTemplateProxy>, ReplacementConstructor>
  replacementConstructors;

  /**
   * Create a new instance.
   *
   * @param replacements A collection of replacing template classes.
   * @throws IllegalArgumentException if any of the passed in classes do not correspond to templates
   *           which replace another template, or if two replacing templates replace the same
   *           template.
   */
  public FixedTemplateReplacer(Collection<Class<? extends AbstractTemplateProxy>> replacements) {
    replacementConstructors =
      new HashMap<Class<? extends AbstractTemplateProxy>, ReplacementConstructor>(
          replacements.size());
    for (Class<? extends AbstractTemplateProxy> replacingTemplate : replacements) {
      Replaces replaces = replacingTemplate.getAnnotation(Replaces.class);
      if (replaces == null) {
        throw new IllegalArgumentException(
          "Provided replacement template " + replacingTemplate.getName()
          + " is not declared to replace anything");
      }
      Class<? extends AbstractTemplateProxy> replacedTemplate = replaces.replacedProxy();
      if (!replacedTemplate.isAnnotationPresent(Replaceable.class)) {
        throw new IllegalArgumentException(
          "Template " + replacedTemplate.getName() + " is not replaceable");
      }
      try {
        ReplacementConstructor previousReplacementConstructor = replacementConstructors.put(
          replacedTemplate, replaces.replacementConstructor().newInstance());
        if (previousReplacementConstructor != null) {
          throw new IllegalArgumentException(
            "Template " + replacedTemplate.getName() + " is replaced by both "
            + previousReplacementConstructor.getClass().getEnclosingClass().getName() + " and "
            + replaces.replacementConstructor().getClass().getEnclosingClass().getName());
        }
      }
      catch (InstantiationException e) {
        throw new RuntimeException(e);
      }
      catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  protected ReplacementConstructor findReplacement(
    Class<? extends AbstractTemplateProxy> proxyClass, Object jamonContext) {
    return replacementConstructors.get(proxyClass);
  }

}
