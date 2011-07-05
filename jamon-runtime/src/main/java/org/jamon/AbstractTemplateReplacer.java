/*
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2011.
 *
 * The Initial Developer of the Original Code is Ian Robertson. Portions created
 * by Ian Robertson are Copyright (C) 2011 Ian Robertson. All Rights Reserved.
 *
 * Contributor(s):
 */

package org.jamon;

import org.jamon.AbstractTemplateProxy.ImplData;
import org.jamon.AbstractTemplateProxy.ImplDataCompatible;
import org.jamon.AbstractTemplateProxy.ReplacementConstructor;

/**
 * A base class for classes wishing to define a {@code TemplateReplacer}.
 * Implementors need only define {@link #findReplacement(Class, Object)}
 */
public abstract class AbstractTemplateReplacer implements TemplateReplacer
{
    @Override
    public AbstractTemplateProxy getReplacement(AbstractTemplateProxy p_proxy,
        Object p_jamonContext)
    {
        ReplacementConstructor constructor = findReplacement(p_proxy.getClass(), p_jamonContext);
        if (constructor != null)
        {
            AbstractTemplateProxy replacedProxy = constructor.makeReplacement();
            @SuppressWarnings("unchecked")
            ImplDataCompatible<ImplData> replacedImplData =
                (ImplDataCompatible<ImplData>) replacedProxy.getImplData();
            replacedImplData.populateFrom(p_proxy.getImplData());
            return replacedProxy;
        }
        else
        {
            return p_proxy;
        }
    }

    /**
     * Find an appropriate {@link ReplacementConstructor} for a template, if
     * there is one.
     *
     * @param p_proxyClass the class to find a replacement for.
     * @param p_jamonContext the jamonContext
     * @return the {@code ReplacementConstructor} for the template which will
     *         serve as a replacement, or {@code null} if there is to be no
     *         replacement performed.
     */
    protected abstract ReplacementConstructor findReplacement(
        Class<? extends AbstractTemplateProxy> p_proxyClass, Object p_jamonContext);

}
