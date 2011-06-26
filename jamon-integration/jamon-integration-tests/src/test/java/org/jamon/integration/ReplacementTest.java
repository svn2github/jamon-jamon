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
 * The Original Code is Jamon code, released February, 2011.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2011 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.integration;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.Map;

import org.jamon.AbstractTemplateProxy;
import org.jamon.AbstractTemplateReplacer;
import org.jamon.BasicTemplateManager;
import org.jamon.TemplateManager;
import org.jamon.TemplateReplacer;
import org.jamon.AbstractTemplateProxy.ReplacementConstructor;
import org.jamon.annotations.Replaceable;
import org.jamon.annotations.Replaces;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import test.jamon.replacement.Api;
import test.jamon.replacement.ApiChildReplacementCaller;
import test.jamon.replacement.ApiReplacement;
import test.jamon.replacement.ApiReplacementChild;
import test.jamon.replacement.ApiWithFargs;
import test.jamon.replacement.ApiWithFargsCaller;
import test.jamon.replacement.ApiWithFargsReplacementChild;
import test.jamon.replacement.GenericApiCaller;
import test.jamon.replacement.i18n.GreetingCaller;

public class ReplacementTest
{
    private final static TemplateReplacer TEMPLATE_REPLACER =
        new AbstractTemplateReplacer() {
        @Override
        protected ReplacementConstructor findReplacement(
            Class<?> p_proxyClass, Object p_jamonContext)
        {
            try
            {
                return p_proxyClass.getClassLoader().loadClass(p_proxyClass.getName() + "Replacement")
                .getAnnotation(Replaces.class).replacementConstructor().newInstance();
            }
            catch (ClassNotFoundException e)
            {
                return null;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    };


    private final static TemplateReplacer I18N_TEMPLATE_REPLACER = new AbstractTemplateReplacer()
    {
        @Override
        protected ReplacementConstructor findReplacement(
            Class<?> p_proxyClass, Object p_jamonContext)
        {
            if (! (p_jamonContext instanceof Locale))
            {
                return null;
            }
            else
            {
                String lang = ((Locale) p_jamonContext).getLanguage();
                try {
                    return Class.forName(p_proxyClass.getName()+ "_" + lang)
                    .getAnnotation(Replaces.class).replacementConstructor().newInstance();
                }
                catch (Exception e) {
                    return null;
                }
            }
        }
    };

    private final static class SimpleTemplateReplacer extends AbstractTemplateReplacer
    {
        private final Map<Class<? extends AbstractTemplateProxy>, ReplacementConstructor> m_replacements;

        public SimpleTemplateReplacer(
            Class<? extends AbstractTemplateProxy> p_replaced,
            Class<? extends AbstractTemplateProxy> p_replacement) throws Exception {
            this(ImmutableMap.<Class<? extends AbstractTemplateProxy>,
                               Class<? extends AbstractTemplateProxy>>of(
                                   p_replaced, p_replacement));
        }

        private SimpleTemplateReplacer(
            Map<Class<? extends AbstractTemplateProxy>,
            Class<? extends AbstractTemplateProxy>> p_replacements) throws Exception
        {
            m_replacements = Maps.newHashMap();
            for (Map.Entry<
                    Class<? extends AbstractTemplateProxy>,
                    Class<? extends AbstractTemplateProxy>> entry: p_replacements.entrySet()) {
                m_replacements.put(
                    entry.getKey(),
                    entry.getValue().getAnnotation(Replaces.class)
                        .replacementConstructor().newInstance());
            }
        }

        @Override
        protected ReplacementConstructor findReplacement(Class<?> p_proxyClass, Object p_jamonContext)
        {
            return m_replacements.get(p_proxyClass);
        }


    }

    @Test
    public void testSimpleReplacement() throws Exception
    {
        assertEquals("Replacement: 3 4", new Api(manager(TEMPLATE_REPLACER)).makeRenderer(3, 4).asString());
    }

    @Test
    public void testReplacementWithFrags() throws Exception
    {
        assertEquals(
            "Implementor got 3",
            new ApiWithFargsCaller(manager(TEMPLATE_REPLACER)).makeRenderer().asString());
    }

    @Test
    public void testReplacementWithFragsFromParent() throws Exception
    {
      TemplateReplacer templateReplacer = new AbstractTemplateReplacer() {
        @Override
        protected ReplacementConstructor findReplacement(
            Class<?> p_proxyClass, Object p_jamonContext)
        {
          if (p_proxyClass == ApiWithFargs.class)
          {
            return new ApiWithFargsReplacementChild.ReplacementConstructor();
          }
          else
          {
            return null;
          }
        }
      };

      assertEquals(
        "Implementor got 3\nImplementor got 4",
        new ApiWithFargsCaller(manager(templateReplacer)).makeRenderer().asString());
    }

    @Test
    public void testReplacementWithGenerics() throws Exception
    {
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
            new Api(manager(new SimpleTemplateReplacer(Api.class, ApiReplacementChild.class)))
                .makeRenderer(3, 4).asString());
    }

    @Test
    public void testi18nReplacement() throws Exception
    {
        assertEquals(
            "Hello, Jamon tester",
            new GreetingCaller(manager(I18N_TEMPLATE_REPLACER)).makeRenderer().asString());
        assertEquals(
            "Hello, Jamon tester",
            new GreetingCaller(manager(I18N_TEMPLATE_REPLACER)).setJamonContext(Locale.ENGLISH).makeRenderer().asString());
        assertEquals(
            "Guten Tag, Jamon tester",
            new GreetingCaller(manager(I18N_TEMPLATE_REPLACER)).setJamonContext(Locale.GERMAN).makeRenderer().asString());
    }

    @Test
    public void testAnnotations() throws Exception
    {
        assertTrue(Api.class.isAnnotationPresent(Replaceable.class));
        Replaces replaces = ApiReplacement.class.getAnnotation(Replaces.class);
        assertNotNull(replaces);
        assertEquals(Api.class, replaces.replacedProxy());
        assertEquals(
            ApiReplacement.ReplacementConstructor.class, replaces.replacementConstructor());
    }

    private TemplateManager manager(TemplateReplacer p_templateReplacer)
    {
        return new BasicTemplateManager(getClass().getClassLoader(), p_templateReplacer);
    }
}
