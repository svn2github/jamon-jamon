import java.io.OutputStreamWriter;
import java.util.Date;
import java.math.BigDecimal;

public class AccountSummaryTut3 {
  public static void main(String[] argv) throws Exception {
    new AccountSummaryTemplate()
      .setTitle("Most Recent Account Balances")
      .render(new OutputStreamWriter(System.out),
              new Date(), "John Public", new BigDecimal(9.99));
  }
}
