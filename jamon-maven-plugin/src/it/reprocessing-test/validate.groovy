import static org.jamon.test.ReprocessingTestConstants.*
import static org.jamon.test.TemplateBundle.Type.*

def verifyBundleTimeStamps = { bundle, expectedTimestampDelta ->
  templateTimestamp = bundle.templateTimestamp()
  [PROXY, IMPL].each {
    file = bundle.get(it)
    delta = file.lastModified() - templateTimestamp;
    assert delta >= 0
    println("checking timestamp for $file")
    assert Math.abs(delta - expectedTimestampDelta) < 60 * 1000
  }
}

verifyBundleTimeStamps(reprocess(basedir), 0)
verifyBundleTimeStamps(onlyImplProcessed(basedir), 0)
verifyBundleTimeStamps(onlyProxyProcessed(basedir), 0)
verifyBundleTimeStamps(alreadyProcessed(basedir), 1000L * 60 * 60)

true