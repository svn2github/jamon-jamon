package org.jamon.test;

import java.io.File;

public class TemplateBundle {
  private static final String GENERATED_SOURCES_PATH = "target/generated-sources/jamon/";

  public final File templateFile;
  public final File proxyFile;
  public final File implFile;

  public TemplateBundle(File basedir, String path) {
    templateFile = new File(basedir, "src/main/java/" + path + ".jamon");
    proxyFile = new File(basedir, GENERATED_SOURCES_PATH + path + ".java");
    implFile = new File(basedir, GENERATED_SOURCES_PATH + path + "Impl.java");
  }


}
