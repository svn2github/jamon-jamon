/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.jamon.codegen;

import org.jamon.api.SourceGenerator;
import org.jamon.node.AnnotationNode;

public abstract class AbstractSourceGenerator implements SourceGenerator {
  protected AbstractSourceGenerator(TemplateDescriber describer, TemplateUnit templateUnit) {
    this.describer = describer;
    this.templateUnit = templateUnit;
  }

  protected final TemplateDescriber describer;

  protected CodeWriter writer;

  protected final TemplateUnit templateUnit;

  protected void generateCustomAnnotations(
    Iterable<AnnotationNode> annotationNodes, AnnotationType annotationType) {
    for (AnnotationNode annotationNode : annotationNodes) {
      if (annotationNode.getType() == AnnotationType.BOTH
        || annotationNode.getType() == annotationType) {
        writer.printLocation(annotationNode.getLocation());
        writer.println(annotationNode.getAnnotations());
      }
    }
  }
}
