package org.jamon.codegen;

import org.jamon.node.AnnotationNode;

public abstract class AbstractSourceGenerator implements SourceGenerator
{
    protected AbstractSourceGenerator(TemplateDescriber p_describer, TemplateUnit p_templateUnit)
    {
        m_describer = p_describer;
        m_templateUnit = p_templateUnit;
    }

    protected final TemplateDescriber m_describer;
    protected CodeWriter m_writer;
    protected final TemplateUnit m_templateUnit;

    protected void generateCustomAnnotations(
        Iterable<AnnotationNode> p_annotationNodes, AnnotationType p_annotationType)
    {
        for (AnnotationNode annotationNode: p_annotationNodes)
        {
            if (annotationNode.getType() == AnnotationType.BOTH
                    || annotationNode.getType() == p_annotationType)
            {
                m_writer.printLocation(annotationNode.getLocation());
                m_writer.println(annotationNode.getAnnotations());
            }
        }
    }
}