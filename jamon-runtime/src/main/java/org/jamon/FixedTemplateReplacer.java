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
 * The Original Code is Jamon code, released July, 2011.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2011 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

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
    private final Map<Class<? extends AbstractTemplateProxy>, ReplacementConstructor>
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
