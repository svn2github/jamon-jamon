import org.jamon.test.TemplateBundle;

def createJavaFile = { file ->
  className = file.getName().replaceAll(".java","");
  file.getParentFile().mkdirs();
  file.write("""\
  package org.jamon;
  public class $className {}
""");
}

def createJavaFiles = { bundle, timestampDelta->
  timeStamp = bundle.templateFile.lastModified() + timestampDelta

  [bundle.proxyFile, bundle.implFile].each {
    createJavaFile(it)
    it.setLastModified(timeStamp)
  }
}

def alreadyProcessed = new TemplateBundle(basedir, 'org/jamon/AlreadyProcessed')
def reprocess = new TemplateBundle(basedir, 'org/jamon/Reprocess')

def hour = 1000L * 60 * 60;

createJavaFiles(alreadyProcessed, hour)
createJavaFiles(reprocess, -hour)

return true;
