import java.io.OutputStreamWriter;
import java.util.Date;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class TimeAndTempTut2 {
  public static void main(String[] argv) {
    try {
      // set the template manager once for all time ...
      TemplateManagerSource
          .setTemplateManager(new StandardTemplateManager());
      // now create templates without specifying a template manager
      new TimeAndTempTemplate()
          .render(new OutputStreamWriter(System.out));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
