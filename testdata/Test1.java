package test.jtt;

import java.io.*;
import java.math.*;
import org.modusponens.jtt.*;

public class Test1
{
    public static void main(String [] args)
    {
        try
        {
            Writer w = new OutputStreamWriter(System.out);
            TemplateManager m = new StandardTemplateManager();
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
