package org.jamon.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * A reflection tool to convert annotations loaded under a different class loader into an annotation
 * implementing a specified annotation class. Because TemplateDescription is working with classes
 * loaded from a client-defined ClassLoader, it is possible that said ClassLoader will contain it's
 * own copy of jamon.jar, meaning that annotations we load from classes it returns will not be class
 * compatible with our copies of the annotation classes.
 */
public class AnnotationReflector {
  private Map<String, Annotation> annotations = new HashMap<String, Annotation>();

  public AnnotationReflector(Class<?> clazz) {
    for (Annotation annotation : clazz.getAnnotations()) {
      annotations.put(annotation.annotationType().getName(), annotation);
    }
  }

  public <T extends Annotation> T getAnnotation(Class<T> clazz) {
    final Annotation annotation = annotations.get(clazz.getName());
    return clazz.cast(proxyAnnotation(clazz, annotation));
  }

  private Object proxyAnnotation(Class<?> clazz, final Object annotation) {
    return clazz.cast(Proxy.newProxyInstance(getClass().getClassLoader(),
      new Class<?>[] { clazz }, new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          Object result = annotation.getClass().getMethod(method.getName()).invoke(annotation);
          return maybeProxyAnnotation(method.getReturnType(), result);
        }

      }));
  }

  private Object maybeProxyAnnotation(Class<?> type, Object object) {
    if (object == null) {
      return null;
    }

    if (type.isAnnotation()) {
      return proxyAnnotation(type, object);
    }
    if (type.isArray() && type.getComponentType().isAnnotation()) {
      int arrayLength = Array.getLength(object);
      Object array = Array.newInstance(type.getComponentType(), arrayLength);
      for (int i = 0; i < arrayLength; i++) {
        Array.set(array, i, proxyAnnotation(type.getComponentType(), Array.get(object, i)));
      }
      return array;
    }
    else {
      return object;
    }
  }
}
