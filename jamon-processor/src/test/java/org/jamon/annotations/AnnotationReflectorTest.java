package org.jamon.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;

public class AnnotationReflectorTest
{
    @Template(
        signature="abc",
        requiredArguments = {@Argument(name="foo", type="bar")},
        methods = {@Method(name="m", requiredArguments = {@Argument(name="n", type="t")})})
    public static class Example {}

    @Test public void testGetAnnotation() throws Exception
    {
        ClassLoader alternateClassLoader = makeAlternateClassLoader();

        assertNotSame(
            Template.class.getClassLoader(),
            alternateClassLoader.loadClass(Template.class.getName()));

        Class<?> alternateExample = alternateClassLoader.loadClass(Example.class.getName());
        assertNotSame(Example.class.getClassLoader(), alternateExample.getClassLoader());
        AnnotationReflector reflector = new AnnotationReflector(alternateExample);
        Template template = reflector.getAnnotation(Template.class);
        assertEquals("abc", template.signature());
        assertEquals("foo", template.requiredArguments()[0].name());
        assertEquals(1, template.methods().length);
        Method method = template.methods()[0];
        assertEquals("m", method.name());
        assertEquals("n", method.requiredArguments()[0].name());
    }

    private ClassLoader makeAlternateClassLoader() throws MalformedURLException
    {
        ClassLoader exampleClassLoader = Example.class.getClassLoader();
        URL[] urls = null;
//        if (exampleClassLoader instanceof AntClassLoader)
//        {
//            String classPath = ((AntClassLoader) exampleClassLoader).getClasspath();
//            List<URL> urlList = new ArrayList<URL>();
//            for (StringTokenizer tokenizer = new StringTokenizer(classPath, ":");
//                 tokenizer.hasMoreTokens(); )
//            {
//                String pathElement = tokenizer.nextToken();
//                urlList.add(new URL(
//                    "file:" + pathElement + (pathElement.endsWith(".jar") ? "" : "/")));
//            }
//            urls = urlList.toArray(new URL[0]);
//        }
//        else 
          if (exampleClassLoader instanceof URLClassLoader)
        {
            urls = ((URLClassLoader) exampleClassLoader).getURLs();
        }
        ClassLoader alternateClassLoader = new URLClassLoader(urls, exampleClassLoader.getParent());
        return alternateClassLoader;
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(AnnotationReflectorTest.class);
    }
}
