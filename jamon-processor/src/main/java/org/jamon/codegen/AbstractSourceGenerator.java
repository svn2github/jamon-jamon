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
