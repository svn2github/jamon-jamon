package test.jamon;

import foo.bar.test.jamon.TestTemplate;
import java.io.*;
import java.math.*;
import org.jamon.*;

public class Test1
{
    public static void main(String [] args)
    {
        try
        {
            Writer w = new OutputStreamWriter(System.out);
            StandardTemplateManager m =
                new StandardTemplateManager("templates",
                                            "build/work");
            m.setPackagePrefix("foo.bar.");
            TestTemplate.Factory f = new TestTemplate.Factory(m);
            TestTemplate t = f.getInstance(w);
            t.setX(57);
            t.render(new BigDecimal("34.5324"));
            w.flush();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
