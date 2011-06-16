package org.jamon.compatability;

import static org.junit.Assert.*;

import org.jamon.api.TemplateSource;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.TemplateDescription;
import org.junit.Test;
import org.mockito.Mockito;

public class TemplateDescriberTest {
  @Test
  public void testDescription() throws Exception {
    TemplateSource templateSource = Mockito.mock(TemplateSource.class);
    TemplateDescription templateDescription =
      new TemplateDescriber(templateSource, getClass().getClassLoader())
      .getTemplateDescription("/org/jamon/logic/Iterate", null);
    assertFalse(templateDescription.isReplaceable());
  }
}
