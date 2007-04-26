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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.codegen;

public interface ClassNames
{
    public final static String IOEXCEPTION =
        java.io.IOException.class.getName();
    public final static String WRITER = java.io.Writer.class.getName();
    public final static String RENDERER = org.jamon.runtime.Renderer.class.getName();
    public final static String ABSTRACT_RENDERER =
        org.jamon.runtime.AbstractRenderer.class.getName();
    public final static String TEMPLATE =
        org.jamon.runtime.AbstractTemplateProxy.class.getName();
    public final static String TEMPLATE_INTF = TEMPLATE + ".Intf";
    public final static String IMPL_DATA = TEMPLATE + ".ImplData";
    public final static String TEMPLATE_MANAGER =
        org.jamon.runtime.TemplateManager.class.getName();
    public static final String BASE_TEMPLATE =
        org.jamon.runtime.AbstractTemplateImpl.class.getName();
    public static final String ARGUMENT_ANNOTATION =
        org.jamon.annotations.Argument.class.getName();
    public static final String FRAGMENT_ANNOTATION =
        org.jamon.annotations.Fragment.class.getName();
    public static final String METHOD_ANNOTATION =
        org.jamon.annotations.Method.class.getName();
    public static final String TEMPLATE_ANNOTATION =
        org.jamon.annotations.Template.class.getName();
}
