import java.io.OutputStreamWriter;
import java.util.Date;
import java.math.BigDecimal;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class AccountSummaryTut3 {
  public static void main(String[] argv) {
    try {
      // set the template manager once for all time ...
      TemplateManagerSource
          .setTemplateManager(new StandardTemplateManager());
      // now create templates without specifying a template manager
      new AccountSummaryTemplate()
          .setTitle("Most Recent Account Balances")
          .render(new OutputStreamWriter(System.out),
                  new Date(), "John Public", new BigDecimal(9.99));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
