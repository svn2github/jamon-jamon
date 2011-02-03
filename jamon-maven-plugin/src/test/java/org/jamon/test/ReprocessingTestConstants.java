package org.jamon.test;

import java.io.File;

public class ReprocessingTestConstants {
  private ReprocessingTestConstants() {}

  public static long HOUR = 1000L * 60 * 60;

  public static TemplateBundle alreadyProcessed(File basedir) {
    return new TemplateBundle(basedir, "org/jamon/AlreadyProcessed");
  }

  public static TemplateBundle onlyImplProcessed(File basedir) {
    return new TemplateBundle(basedir, "org/jamon/OnlyImplProcessed");
  }

  public static TemplateBundle onlyProxyProcessed(File basedir) {
    return new TemplateBundle(basedir, "org/jamon/OnlyProxyProcessed");
  }

  public static TemplateBundle reprocess(File basedir) {
    return new TemplateBundle(basedir, "org/jamon/Reprocess");
  }

}
