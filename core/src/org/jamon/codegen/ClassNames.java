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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2002 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import java.io.Writer;
import java.io.IOException;
import org.jamon.Renderer;
import org.jamon.AbstractTemplateProxy;
import org.jamon.AbstractTemplateImpl;

public interface ClassNames
{
    public final static String IOEXCEPTION =
        IOException.class.getName();
    public final static String WRITER =
        Writer.class.getName();
    public final static String RENDERER =
        org.jamon.Renderer.class.getName();
    public final static String TEMPLATE =
         org.jamon.AbstractTemplateProxy.class.getName();
    public final static String TEMPLATE_INTF = TEMPLATE + ".Intf";
    public final static String TEMPLATE_MANAGER =
        org.jamon.TemplateManager.class.getName();
    public static final String BASE_TEMPLATE =
        org.jamon.AbstractTemplateImpl.class.getName();


}
