package org.jamon.integration;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.jamon.AbstractReplacingTemplateManager;
import org.jamon.AbstractTemplateProxy;
import org.jamon.TemplateManager;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import test.jamon.replacement.Api;
import test.jamon.replacement.ApiChildReplacementCaller;
import test.jamon.replacement.ApiReplacementChild;
import test.jamon.replacement.ApiWithFargsCaller;
import test.jamon.replacement.GenericApiCaller;

public class ReplacementTest
{
    private final static TemplateManager TEMPLATE_MANAGER =
        new AbstractReplacingTemplateManager() {
        @Override
        protected Class<? extends AbstractTemplateProxy> findReplacement(Class<?> p_proxyClass)
        {
            try
            {
                return p_proxyClass.getClassLoader().loadClass(p_proxyClass.getName() + "Replacement")
                    .asSubclass(AbstractTemplateProxy.class);
            }
            catch (ClassNotFoundException e)
            {
                return null;
            }
        }
    };

    private final static class ReplacingTemplateManager
    extends AbstractReplacingTemplateManager
    {
        private final Map<Class<? extends AbstractTemplateProxy>, Class<? extends AbstractTemplateProxy>> m_replacements;

        public ReplacingTemplateManager(
            Class<? extends AbstractTemplateProxy> p_replaced,
            Class<? extends AbstractTemplateProxy> p_replacement) {
            this(ImmutableMap.<Class<? extends AbstractTemplateProxy>,
                               Class<? extends AbstractTemplateProxy>>of(
                                   p_replaced, p_replacement));
        }

        private ReplacingTemplateManager(
            Map<Class<? extends AbstractTemplateProxy>, Class<? extends AbstractTemplateProxy>> p_replacements)
        {
            m_replacements = p_replacements;
        }

        @Override
        protected Class<? extends AbstractTemplateProxy> findReplacement(Class<?> p_proxyClass)
        {
            return m_replacements.get(p_proxyClass);
        }


    }

    @Test
    public void testSimpleReplacement() throws Exception
    {
        assertEquals("Replacement: 3 4", new Api(TEMPLATE_MANAGER).makeRenderer(3, 4).asString());
    }

    @Test
    public void testReplacementWithFrags() throws Exception
    {
        assertEquals(
            "Implementor got 3",
            new ApiWithFargsCaller(TEMPLATE_MANAGER).makeRenderer().asString());
    }

    @Test
    public void testReplacementWithGenerics() throws Exception
    {
        assertEquals(
            "|x||y|",
            new GenericApiCaller(TEMPLATE_MANAGER).makeRenderer().asString());
    }

    @Test
    public void testReplacementOfChildTemplate() throws Exception {
        assertEquals(
            "s1: hello\ns2: t\ni1: 1\ni2: 5\nfragment f\nfragment g",
            new ApiChildReplacementCaller(TEMPLATE_MANAGER).makeRenderer().asString());
    }

    @Test
    public void testReplacementOfTemplateWithChildTemplate() throws Exception {
        assertEquals(
            "Parent: 3 Child: 4",
            new Api(new ReplacingTemplateManager(Api.class, ApiReplacementChild.class))
                .makeRenderer(3, 4).asString());
    }
}
