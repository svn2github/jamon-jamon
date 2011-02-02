import org.jamon.test.TemplateBundle;

def alreadyProcessed = new TemplateBundle(basedir, 'org/jamon/AlreadyProcessed')
def reprocess = new TemplateBundle(basedir, 'org/jamon/Reprocess')

def verifyBundleTimeStamps = { bundle, expectedTimestampDelta ->
  templateTimestamp = bundle.templateFile.lastModified()
  [bundle.proxyFile, bundle.implFile].each {
    delta = it.lastModified() - templateTimestamp;
    assert delta >= 0
    assert Math.abs(delta - expectedTimestampDelta) < 60 * 1000;
  }
}

verifyBundleTimeStamps(alreadyProcessed, 0)
verifyBundleTimeStamps(reprocess, 0) // 1000L * 60 * 60)

true