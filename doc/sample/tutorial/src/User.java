import java.io.OutputStreamWriter;

public class User
{
  private String name;
  private boolean authorized;

  public User(String p_name, boolean p_authorized) {
    name = p_name;
    authorized = p_authorized;
  }

  public String getName() { return name; }
  public boolean isAuthorized() { return authorized; }

  public static void main(String[] args) throws Exception {
    User[] users = new User[2];
    users[0] = new User("John Public", false);
    users[1] = new User("John Rockerfeller", true);

    for (int i=0; i<users.length; i++) {
      // call render() on the parent template and provide arguments
      processUser(args.length == 0 ? null : args[0])
        .setTitle("Jamon News")  // set optional argument in parent
        .render(new OutputStreamWriter(System.out), users[i]);
    }
  }

  private static InheritanceParentWArgs.ParentRenderer
    processUser(String templateName) {
    // the default template, the special of the week
    if (templateName == null || "special".equalsIgnoreCase(templateName)) {
      return new InheritanceChildWArgs()
        .makeParentRenderer("Jamon free this week only!");
    } else {
      // process other derived templates here
      return null;
    }
  }
}
