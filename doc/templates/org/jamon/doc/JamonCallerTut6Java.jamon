import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class JamonCallerTut6 {
  public static void main(String[] argv) {
    try {
       String[] names = new String[] {
        "John Public", "Mary Private", "Lee Protected"};
       String[] phoneNumbers = new String[] {
        "7750324", "4135549232", "4135551212" };
       TemplateManagerSource.setTemplateManager(new StandardTemplateManager());
       new JamonCallerTemplate()
           .render(new OutputStreamWriter(System.out), names, phoneNumbers);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
