import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class JamonCallerTut6 {
  public static void main(String[] argv) throws Exception {
    String[] names = new String[] {
      "John Public", "Mary Private", "Lee Protected"};
    String[] phoneNumbers = new String[] {
      "5550324", "4135559232", "4135551212" };
    TemplateManagerSource.setTemplateManager(new StandardTemplateManager());
    new JamonCallerTemplate()
      .render(new OutputStreamWriter(System.out), names, phoneNumbers);
  }
}
