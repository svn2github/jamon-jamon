/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.jamon.api;

import java.util.Collection;

/**
 * A parsed version of a template which is capable of creating proxy and impl files.
 */
public interface ParsedTemplate {
  /**
   * @return the paths of templates which this template depends on.
   */
  Collection<String> getTemplateDependencies();

  /**
   * @return a generator for the proxy.
   */
  SourceGenerator getProxyGenerator();

  /**
   * @return a generator for the impl.
   */
  SourceGenerator getImplGenerator();
}
