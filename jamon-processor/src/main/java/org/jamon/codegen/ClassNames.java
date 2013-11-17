/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.codegen;

import org.jamon.AbstractTemplateProxy;

public interface ClassNames {
  public final static String IOEXCEPTION = java.io.IOException.class.getName();
  public final static String WRITER = java.io.Writer.class.getName();
  public final static String RENDERER = org.jamon.Renderer.class.getName();
  public final static String ABSTRACT_RENDERER = org.jamon.AbstractRenderer.class.getName();
  public final static String TEMPLATE = org.jamon.AbstractTemplateProxy.class.getName();
  public final static String TEMPLATE_INTF = AbstractTemplateProxy.Intf.class.getCanonicalName();
  public final static String IMPL_DATA = AbstractTemplateProxy.ImplData.class.getCanonicalName();
  public final static String IMPL_DATA_COMPATIBLE =
    AbstractTemplateProxy.ImplDataCompatible.class.getCanonicalName();
  public final static String TEMPLATE_MANAGER = org.jamon.TemplateManager.class.getName();
  public static final String BASE_TEMPLATE = org.jamon.AbstractTemplateImpl.class.getName();
  public static final String ARGUMENT_ANNOTATION = org.jamon.annotations.Argument.class.getName();
  public static final String FRAGMENT_ANNOTATION = org.jamon.annotations.Fragment.class.getName();
  public static final String METHOD_ANNOTATION = org.jamon.annotations.Method.class.getName();
  public static final String TEMPLATE_ANNOTATION = org.jamon.annotations.Template.class.getName();
  public static final String REPLACEABLE = org.jamon.annotations.Replaceable.class.getName();
  public static final String REPLACES = org.jamon.annotations.Replaces.class.getName();
  public static final String REPLACEMENT_CONSTRUCTOR =
    AbstractTemplateProxy.ReplacementConstructor.class.getCanonicalName();
}
