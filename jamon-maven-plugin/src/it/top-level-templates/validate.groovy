try {
  assert new File(basedir, 'target/generated-sources/jamon/TestTemplate.java').exists()
  assert new File(basedir, 'target/generated-sources/jamon/TestTemplateImpl.java').exists()
  assert new File(basedir, 'target/classes/TestTemplate.class').exists()
  assert new File(basedir, 'target/classes/TestTemplateImpl.class').exists()
  return true;
}
catch(Throwable e) {
  e.printStackTrace()
  return false;
}
