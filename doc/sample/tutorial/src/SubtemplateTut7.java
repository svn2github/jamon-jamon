import java.io.OutputStreamWriter;

public class SubtemplateTut7 {
  public static void main(String[] argv) throws Exception {
    // data to pass to the template
    String[] accountNames = new String[] {
      "John Doe", "Mary Jane", "Bonnie Blue", "Johnny Reb" };
    String accountInfoUrl = "http://www.bank.com/accountInfo";
    // call the template ...
    new SubtemplateTemplate()
      .setCgiParamName("username")
      .render(new OutputStreamWriter(System.out),
              accountNames, accountInfoUrl);
  }
}
