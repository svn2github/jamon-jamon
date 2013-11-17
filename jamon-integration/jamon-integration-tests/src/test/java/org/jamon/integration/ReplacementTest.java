/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.integration;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Locale;

import org.jamon.AbstractTemplateProxy;
import org.jamon.AbstractTemplateReplacer;
import org.jamon.BasicTemplateManager;
import org.jamon.FixedTemplateReplacer;
import org.jamon.TemplateManager;
import org.jamon.TemplateReplacer;
import org.jamon.AbstractTemplateProxy.ReplacementConstructor;
import org.jamon.annotations.Replaceable;
import org.jamon.annotations.Replaces;
import org.junit.Test;

import test.jamon.replacement.Api;
import test.jamon.replacement.ApiChildReplacementCaller;
import test.jamon.replacement.ApiReplacement;
import test.jamon.replacement.ApiReplacementChild;
import test.jamon.replacement.ApiWithFargsCaller;
import test.jamon.replacement.ApiWithFargsReplacementChild;
import test.jamon.replacement.GenericApiCaller;
import test.jamon.replacement.context.ApiWithContext;
import test.jamon.replacement.i18n.GreetingCaller;

public class ReplacementTest extends TestBase {
  private final static TemplateReplacer TEMPLATE_REPLACER = new AbstractTemplateReplacer() {
    @Override
    protected ReplacementConstructor findReplacement(
      Class<? extends AbstractTemplateProxy> proxyClass, Object jamonContext) {
      try {
        return proxyClass
          .getClassLoader()
          .loadClass(proxyClass.getName() + "Replacement")
          .getAnnotation(Replaces.class)
          .replacementConstructor()
          .newInstance();
      }
      catch (ClassNotFoundException e) {
        return null;
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  };

  private final static TemplateReplacer I18N_TEMPLATE_REPLACER = new AbstractTemplateReplacer() {
    @Override
    protected ReplacementConstructor findReplacement(
      Class<? extends AbstractTemplateProxy> proxyClass, Object jamonContext) {
      if (!(jamonContext instanceof Locale)) {
        return null;
      }
      else {
        String lang = ((Locale) jamonContext).getLanguage();
        try {
          return Class.forName(proxyClass.getName() + "_" + lang)
            .getAnnotation(Replaces.class)
            .replacementConstructor()
            .newInstance();
        }
        catch (Exception e) {
          return null;
        }
      }
    }
  };

  @SuppressWarnings("unchecked")
  private static TemplateReplacer replacer(Class<? extends AbstractTemplateProxy> replacement) {
    return new FixedTemplateReplacer(
      Arrays.<Class<? extends AbstractTemplateProxy>> asList(replacement));
  }

  @Test
  public void testSimpleReplacement() throws Exception {
    assertEquals(
      "Replacement: 3 4",
      new Api(manager(TEMPLATE_REPLACER)).makeRenderer(3, 4).asString());
  }

  @Test
  public void testReplacementWithFrags() throws Exception {
    assertEquals(
      "Implementor got 3",
      new ApiWithFargsCaller(manager(TEMPLATE_REPLACER)).makeRenderer().asString());
  }

  @Test
  public void testReplacementWithFragsFromParent() throws Exception {
    TemplateReplacer templateReplacer = replacer(ApiWithFargsReplacementChild.class);

    assertEquals(
      "Implementor got 3\nImplementor got 4",
      new ApiWithFargsCaller(manager(templateReplacer)).makeRenderer().asString());
  }

  @Test
  public void testReplacementWithGenerics() throws Exception {
    assertEquals(
      "|1||2|",
      new GenericApiCaller(manager(TEMPLATE_REPLACER)).makeRenderer().asString());
  }

  @Test
  public void testReplacementOfChildTemplate() throws Exception {
    assertEquals(
      "s1: hello\ns2: t\ni1: 1\ni2: 5\nfragment f\nfragment g",
      new ApiChildReplacementCaller(manager(TEMPLATE_REPLACER)).makeRenderer().asString());
  }

  @Test
  public void testReplacementOfTemplateWithChildTemplate() throws Exception {
    assertEquals(
      "Parent: 3 Child: 4",
      new Api(manager(replacer(ApiReplacementChild.class))).makeRenderer(3, 4).asString());
  }

  @Test
  public void testi18nReplacement() throws Exception {
    assertEquals(
      "Hello, Jamon tester",
      new GreetingCaller(manager(I18N_TEMPLATE_REPLACER)).makeRenderer().asString());
    assertEquals(
      "Hello, Jamon tester",
      new GreetingCaller(manager(I18N_TEMPLATE_REPLACER))
        .setJamonContext(Locale.ENGLISH).makeRenderer().asString());
    assertEquals(
      "Guten Tag, Jamon tester",
      new GreetingCaller(manager(I18N_TEMPLATE_REPLACER))
        .setJamonContext(Locale.GERMAN).makeRenderer().asString());
  }

  @Test
  public void testReplacementContext() throws Exception {
    assertEquals(
      "foo",
      new ApiWithContext(manager(TEMPLATE_REPLACER))
        .setJamonContext("foo")
        .makeRenderer()
        .asString());
  }

  @Test
  public void testReplacementWithoutContext() throws Exception {
    expectParserError(
      "replacement/ApiReplacementWithContext",
      "Replaced component does not have a jamonContext, but replacing component has a " +
      "jamonContext of type String",
      1, 1);
  }

  @Test
  public void testAnnotations() throws Exception {
    assertTrue(Api.class.isAnnotationPresent(Replaceable.class));
    Replaces replaces = ApiReplacement.class.getAnnotation(Replaces.class);
    assertNotNull(replaces);
    assertEquals(Api.class, replaces.replacedProxy());
    assertEquals(ApiReplacement.ReplacementConstructor.class, replaces.replacementConstructor());
  }

  private TemplateManager manager(TemplateReplacer p_templateReplacer) {
    return new BasicTemplateManager(getClass().getClassLoader(), p_templateReplacer);
  }
}
