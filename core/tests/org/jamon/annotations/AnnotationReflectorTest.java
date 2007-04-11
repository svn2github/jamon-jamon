package org.jamon.annotations;

import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.AntClassLoader;
import org.junit.Test;

public class AnnotationReflectorTest
{
    @Template(signature="abc", requiredArguments = {@Argument(name="foo", type="bar")})
    public static class Example {}

    @Test public void testGetAnnotation() throws Exception
    {
        ClassLoader exampleClassLoader = Example.class.getClassLoader();
        URL[] urls = null;
        if (exampleClassLoader instanceof AntClassLoader)
        {
            String classPath = ((AntClassLoader) exampleClassLoader).getClasspath();
            List<URL> urlList = new ArrayList<URL>();
            for (StringTokenizer tokenizer = new StringTokenizer(classPath, ":");
                 tokenizer.hasMoreTokens(); )
            {
                String pathElement = tokenizer.nextToken();
                urlList.add(new URL(
                    "file:" + pathElement + (pathElement.endsWith(".jar") ? "" : "/")));
            }
            urls = urlList.toArray(new URL[0]);
        }
        else if (exampleClassLoader instanceof URLClassLoader)
        {
            urls = ((URLClassLoader) exampleClassLoader).getURLs();
        }
        System.out.println(Arrays.asList(urls));
        ClassLoader alternateClassLoader = new URLClassLoader(urls, exampleClassLoader.getParent());

        assertNotSame(
            Template.class.getClassLoader(),
            alternateClassLoader.loadClass(Template.class.getName()));

        Class<?> alternateExample = alternateClassLoader.loadClass(Example.class.getName());
        assertNotSame(Example.class.getClassLoader(), alternateExample.getClassLoader());
        AnnotationReflector reflector = new AnnotationReflector(alternateExample);
        Template template = reflector.getAnnotation(Template.class);
        assertEquals("abc", template.signature());
        System.out.println(template.requiredArguments());
        assertEquals("foo", template.requiredArguments()[0].name());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(AnnotationReflectorTest.class);
    }
}
