package org.jamon.codegen;

import java.util.Collection;

import org.jamon.api.ParsedTemplate;
import org.jamon.api.SourceGenerator;

public class ParsedTemplateImpl implements ParsedTemplate {
  private final TemplateDescriber templateDescriber;

  private final TemplateUnit templateUnit;

  public ParsedTemplateImpl(TemplateDescriber templateDescriber, TemplateUnit templateUnit) {
    this.templateDescriber = templateDescriber;
    this.templateUnit = templateUnit;
  }

  @Override
  public SourceGenerator getImplGenerator() {
    return new ImplGenerator(templateDescriber, templateUnit);
  }

  @Override
  public SourceGenerator getProxyGenerator() {
    return new ProxyGenerator(templateDescriber, templateUnit);
  }

  @Override
  public Collection<String> getTemplateDependencies() {
    return templateUnit.getTemplateDependencies();
  }

}
