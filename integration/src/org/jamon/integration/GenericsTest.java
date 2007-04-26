package org.jamon.integration;

import java.io.Writer;

import org.jamon.runtime.AbstractTemplateProxy;

import test.jamon.GenericsCall;
import test.jamon.NonParameterizedGenericsCall;

public class GenericsTest extends TestBase
{
    public void testExercise() throws Exception
    {
        new GenericsCall().render(getWriter());
        checkOutput("Got: x\nGot: y\n");
    }

    public void testNonParameterizedGenericsCall() throws Exception
    {
       new NonParameterizedGenericsCall().render(getWriter());
       checkOutput("Got: x\nGot: y\n");
    }

    public void testExternalGenericCall() throws Exception
    {
        AbstractTemplateProxy proxy =
            getRecompilingTemplateManager().constructProxy
            ("/test/jamon/external/ExternalGenericCall");
        proxy.getClass()
            .getMethod("render", Writer.class)
            .invoke(proxy, getWriter());
        checkOutput("Got: x\nGot: y\n");
    }

    public void testTypeParamsForSubComponentCall() throws Exception
    {
        expectParserError(
            "TypeParamsForDefCall",
            "def foo is being called with generic parameters", 2, 1);
    }

    public void testAbstractGeneric() throws Exception
    {
        expectParserError(
            "GenericAbstractTemplate",
            "<%generics> tag not allowed in abstract templates", 2, 11);
    }
}
