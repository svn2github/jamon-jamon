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
 * Contributor(s):
 */

package org.jamon.runtime;

/**
 * A <code>TemplateManager</code> is the entry point to obtaining
 * instances of template objects.
 */
public interface TemplateManager
{
    /**
     * Given a proxy, return an instance of the executable code for
     * that proxy's template.
     *
     * @param p_proxy a proxy for the template
     *
     * @return a <code>Template</code> instance
     **/
    AbstractTemplateProxy.Intf constructImpl(AbstractTemplateProxy p_proxy);

    /**
     * Given a template path, return a proxy for that template.
     *
     * @param p_path the path to the template
     *
     * @return a <code>Template</code> instance
     */
    AbstractTemplateProxy constructProxy(String p_path);
}
