try {
  assert new File(basedir, 'target/generated-sources/jamon/org/jamon/TestTemplate.java').exists()
  assert new File(basedir, 'target/generated-sources/jamon/org/jamon/TestTemplateImpl.java').exists()
  assert new File(basedir, 'target/classes/org/jamon/TestTemplate.class').exists()
  assert new File(basedir, 'target/classes/org/jamon/TestTemplateImpl.class').exists()
  return true;
}
catch(Throwable e) {
  e.printStackTrace()
  return false;
}