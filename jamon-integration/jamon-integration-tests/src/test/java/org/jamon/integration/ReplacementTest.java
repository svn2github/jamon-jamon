package org.jamon.integration;

import static org.junit.Assert.assertEquals;

import org.jamon.AbstractReplacingTemplateManager;
import org.jamon.AbstractTemplateProxy;
import org.jamon.TemplateManager;
import org.junit.Test;

import test.jamon.replacement.Api;
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

    @Test
    public void testSimpleReplacement() throws Exception
    {
        assertEquals("Implementor: 3", new Api(TEMPLATE_MANAGER).makeRenderer(3).asString());
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
}
