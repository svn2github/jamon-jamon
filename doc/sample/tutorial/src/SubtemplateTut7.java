import java.io.OutputStreamWriter;
import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;

public class SubtemplateTut7 {
  public static void main(String[] argv) {
    try {
       // data to pass to the template
       String[] accountNames = new String[] {
        "John Doe", "Mary Jane", "Bonnie Blue", "Johnny Reb" };
       String accountInfoUrl = "http://www.bank.com/accountInfo";
       // call the template ...
       TemplateManagerSource
           .setTemplateManager(new StandardTemplateManager());
       new SubtemplateTemplate()
         .setCgiParamName("username")
         .render(new OutputStreamWriter(System.out),
                 accountNames, accountInfoUrl);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
