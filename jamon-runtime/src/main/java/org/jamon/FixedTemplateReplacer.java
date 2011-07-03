package org.jamon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jamon.AbstractTemplateProxy.ReplacementConstructor;
import org.jamon.annotations.Replaceable;
import org.jamon.annotations.Replaces;

/**
 * A {@code TemplateReplacer} which ignores any {@code jamonContext} and replaces templates based on
 * a fixed list of template passed in at startup. While reflection is used when this class is
 * constructed, after that it is reflection-free.
 */
public class FixedTemplateReplacer extends AbstractTemplateReplacer
{
    private Map<Class<? extends AbstractTemplateProxy>, ReplacementConstructor>
        m_replacementConstructors;

    /**
     * Create a new instance.
     * @param p_replacements A collection of replacing template classes.
     * @throws IllegalArgumentException if any of the passed in classes do not correspond to
     * templates which replace another template, or if two replacing
     * templates replace the same template.
     */
    public FixedTemplateReplacer(Collection<Class<? extends AbstractTemplateProxy>>
        p_replacements)
    {
        m_replacementConstructors =
            new HashMap<Class<? extends AbstractTemplateProxy>, ReplacementConstructor>(
                    p_replacements.size());
        for (Class<? extends AbstractTemplateProxy> replacingTemplate: p_replacements)
        {
            Replaces replaces = replacingTemplate.getAnnotation(Replaces.class);
            if (replaces == null)
            {
                throw new IllegalArgumentException(
                    "Provided replacement template " + replacingTemplate.getName()
                    + " is not declared to replace anything");
            }
            Class<? extends AbstractTemplateProxy> replacedTemplate = replaces.replacedProxy();
            if (! replacedTemplate.isAnnotationPresent(Replaceable.class))
            {
                throw new IllegalArgumentException(
                    "Template " + replacedTemplate.getName() + " is not replaceable");
            }
            try
            {
                ReplacementConstructor previousReplacementConstructor = m_replacementConstructors.put(
                    replacedTemplate, replaces.replacementConstructor().newInstance());
                if (previousReplacementConstructor != null)
                {
                    throw new IllegalArgumentException(
                        "Template " + replacedTemplate.getName() + " is replaced by both "
                        + previousReplacementConstructor.getClass().getEnclosingClass().getName()
                        + " and "
                        + replaces.replacementConstructor().getClass().getEnclosingClass().getName());
                }
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected ReplacementConstructor findReplacement(
        Class<? extends AbstractTemplateProxy> p_proxyClass,
        Object p_jamonContext)
    {
        return m_replacementConstructors.get(p_proxyClass);
    }

}
