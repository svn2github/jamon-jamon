package org.jamon.integration;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Map;

import org.jamon.AbstractReplacingTemplateManager;
import org.jamon.AbstractTemplateProxy;
import org.jamon.TemplateManager;
import org.junit.Test;

import com.google.common.collect.Maps;

import test.jamon.replacement.Api;
import test.jamon.replacement.ApiReplacement;
import test.jamon.replacement.ApiWithFargs;
import test.jamon.replacement.ApiWithFargsCaller;
import test.jamon.replacement.ApiWithFargsReplacement;

public class ReplacementTest
{
    private static class ReplaceingTemplateManager
    extends AbstractReplacingTemplateManager {
        private final Map<Class<?>, Class<? extends AbstractTemplateProxy>> m_redirects =
            Maps.newHashMap();

        public ReplaceingTemplateManager map(
            Class<? extends AbstractTemplateProxy> apiClass,
            Class<? extends AbstractTemplateProxy> implementorClass) {
            m_redirects.put(apiClass, implementorClass);
            return this;
        }

        @Override
        protected Class<? extends AbstractTemplateProxy> findReplacement(Class<?> p_proxyClass)
        {
            return m_redirects.get(p_proxyClass);
        }
    }

    @Test
    public void testSimpleReplacement() throws Exception
    {
        TemplateManager templateManager =
            new ReplaceingTemplateManager().map(Api.class, ApiReplacement.class);
        assertEquals("Implementor: 3", new Api(templateManager).makeRenderer(3).asString());
    }

    @Test
    public void testReplacementWithFrags() throws Exception
    {
        TemplateManager templateManager =
            new ReplaceingTemplateManager().map(ApiWithFargs.class, ApiWithFargsReplacement.class);
        assertEquals(
            "Implementor got 3",
            new ApiWithFargsCaller(templateManager).makeRenderer().asString());
    }
}
