/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jamon.AbstractTemplateProxy;

/**
 * Indicates that the annotated class represents a jamon template which can replace another jamon
 * template.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Replaces {
  /**
   * @return the proxy class for the template being replaced.
   */
  Class<? extends AbstractTemplateProxy> replacedProxy();

  Class<? extends AbstractTemplateProxy.ReplacementConstructor> replacementConstructor();
}
