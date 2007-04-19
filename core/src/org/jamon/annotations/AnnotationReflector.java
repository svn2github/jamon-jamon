package org.jamon.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class AnnotationReflector
{
    private Map<String, Annotation> m_annotations = new HashMap<String, Annotation>();
    public AnnotationReflector(Class<?> p_class)
    {
        for (Annotation annotation: p_class.getAnnotations())
        {
            m_annotations.put(annotation.annotationType().getName(), annotation);
        }
    }

    public <T extends Annotation> T getAnnotation(Class<T> p_class)
    {
        final Annotation annotation = m_annotations.get(p_class.getName());
        return p_class.cast(proxyAnnotation(p_class, annotation));
    }

    private Object proxyAnnotation(Class<?> p_class, final Object p_annotation)
    {
        return p_class.cast(Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] {p_class},
            new InvocationHandler()
            {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    Object result =
                        p_annotation.getClass().getMethod(method.getName()).invoke(p_annotation);
                    return maybeProxyAnnotation(method.getReturnType(), result);
                }

            }));
    }

    private Object maybeProxyAnnotation(Class<?> p_type, Object p_object) {
        if (p_object == null) {
            return null;
        }

        if (p_type.isAnnotation())
        {
            return proxyAnnotation(p_type, p_object);
        }
        if (p_type.isArray() && p_type.getComponentType().isAnnotation())
        {
            int arrayLength = Array.getLength(p_object);
            Object array = Array.newInstance(p_type.getComponentType(), arrayLength);
            for (int i = 0; i < arrayLength; i++)
            {
                Array.set(
                    array, i, proxyAnnotation(p_type.getComponentType(), Array.get(p_object, i)));
            }
            return array;
        }
        else
        {
            return p_object;
        }
    }
}
