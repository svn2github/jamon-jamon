import java.io.OutputStreamWriter;
import java.util.Date;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class HelloTut1Alternate {
  public static void main(String[] argv) throws Exception {
    // set the template manager once for all time ...
    TemplateManagerSource.setTemplateManager(new StandardTemplateManager());
    // now create templates without specifying a template manager
    new HelloTemplate().render(new OutputStreamWriter(System.out));
  }
}
