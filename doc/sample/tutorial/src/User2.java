import java.io.OutputStreamWriter;

public class User2
{
  private String name;
  private boolean authorized;

  public User2(String p_name, boolean p_authorized) {
    name = p_name;
    authorized = p_authorized;
  }

  public String getName() { return name; }
  public boolean isAuthorized() { return authorized; }

  public static void main(String[] args) throws Exception {
    User2[] users = new User2[2];
    users[0] = new User2("John Public", false);
    users[1] = new User2("John Rockerfeller", true);

    for (int i=0; i<users.length; i++) {
      // call render() on the parent template and provide arguments
      processUser(args.length == 0 ? null : args[0])
        .render(new OutputStreamWriter(System.out), users[i]);
    }
  }

  /** Chooses the correct derived template to invoke and calls
   *  <code>makeParentRenderer()</code> on that instance.
   *
   *  @param templateName the key name of the template to create
   *  @return the <code>ParentRenderer</code> of the derived template
   */
  private static InheritanceParentProtected.ParentRenderer
    processUser(String templateName) {
    // the default template, the special of the week
    if (templateName == null || "special".equalsIgnoreCase(templateName))
    {
      return new InheritanceChildProtected()
        .makeParentRenderer("Jamon free this week only!");
    } else {
      // process other derived templates here
      return null;
    }
  }
}
