/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
