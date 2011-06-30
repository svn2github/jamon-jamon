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
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon;

/**
 * A <code>TemplateManager</code> is the entry point to obtaining
 * instances of template objects.
 *
 * @see TemplateManagerSource
 */
public interface TemplateManager
{
    /**
     * Given a proxy, return an instance of the executable code for
     * that proxy's template or a suitable replacement.
     *
     * @param p_proxy a proxy for the template
     *
     * @return a <code>Template</code> instance
     **/
    AbstractTemplateProxy.Intf constructImpl(AbstractTemplateProxy p_proxy);

    /**
     * Given a proxy and a jamonContext, return an instance of the executable code for
     * that proxy's template or a suitable replacement, possibly based on the jamonContext.
     *
     * @param p_proxy a proxy for the template
     * @param p_jamonContext the current jamonContext (can be {@code null})
     *
     * @return a <code>Template</code> instance
     **/
    AbstractTemplateProxy.Intf constructImpl(AbstractTemplateProxy p_proxy, Object p_jamonContext);

    /**
     * Given a template path, return a proxy for that template.
     *
     * @param p_path the path to the template
     *
     * @return a <code>Template</code> instance
     */
    AbstractTemplateProxy constructProxy(String p_path);
}
