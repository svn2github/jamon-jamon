package org.jamon.codegen;

import java.io.IOException;

import org.jamon.api.ParsedTemplate;
import org.jamon.api.TemplateParser;
import org.jamon.api.TemplateSource;

public class TemplateParserImpl implements TemplateParser {
  private final TemplateDescriber templateDescriber;

  public TemplateParserImpl(TemplateSource templateSource, ClassLoader classLoader) {
    templateDescriber = new TemplateDescriber(templateSource, classLoader);
  }

  @Override
  public ParsedTemplate parseTemplate(String templatePath) throws IOException {
    return new ParsedTemplateImpl(
      templateDescriber, new Analyzer(templatePath, templateDescriber).analyze());
  }

}
