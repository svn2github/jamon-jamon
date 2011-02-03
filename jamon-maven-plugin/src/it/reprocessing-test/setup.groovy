import static org.jamon.test.ReprocessingTestConstants.*
import static org.jamon.test.TemplateBundle.Type.*

def createJavaFile = { file ->
  className = file.getName().replaceAll(".java","");
  file.getParentFile().mkdirs();
  file.write("""\
  package org.jamon;
  public class $className {}
""");
}

def createJavaFiles = { bundle, types, timestampDelta->
  timeStamp = bundle.templateTimestamp() + timestampDelta

  types.each {
    file = bundle.get(it)
    createJavaFile(file)
    file.setLastModified(timeStamp)
  }
}

createJavaFiles(alreadyProcessed(basedir), [PROXY, IMPL], HOUR)
createJavaFiles(reprocess(basedir), [PROXY, IMPL], -HOUR)
createJavaFiles(onlyImplProcessed(basedir), [IMPL], HOUR)
createJavaFiles(onlyProxyProcessed(basedir), [PROXY], HOUR)

return true;
