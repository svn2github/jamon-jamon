package org.jamon.test;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.jamon.maven.JamonMojo;
import org.junit.Test;

public class MissingTemplateDirectoryTest {
  @Test
  public void missingDirectory() throws Exception {
    JamonMojo jamonMojo = new JamonMojo();
    setTemplateSourceDir(jamonMojo, "/no/such/directory");
    try {
      jamonMojo.execute();
      fail("exception expected");
    }
    catch (MojoExecutionException e) {
      String filePath = File.separator + "no" + File.separator + "such" + File.separator + "directory";
      assertEquals("templateSourceDir " + filePath + " does not exist", e.getMessage());
    }
  }

  @Test
  public void templateDirectoryIsFile() throws Exception {
    JamonMojo jamonMojo = new JamonMojo();
    URL resource = getClass().getClassLoader().getResource(
      getClass().getName().replace('.', '/') + ".class");
    String filePath = new File(resource.toURI()).getAbsolutePath();
    setTemplateSourceDir(jamonMojo, filePath);
    try {
      jamonMojo.execute();
      fail("exception expected");
    }
    catch (MojoExecutionException e) {
      assertEquals("templateSourceDir " + filePath + " is not a directory", e.getMessage());
    }
  }

  private void setTemplateSourceDir(JamonMojo jamonMojo, final String pathname) throws NoSuchFieldException,
    IllegalAccessException {
    Field templateSourceDirField = jamonMojo.getClass().getDeclaredField("templateSourceDir");
    templateSourceDirField.setAccessible(true);
    templateSourceDirField.set(jamonMojo, new File(pathname));
  }
}
