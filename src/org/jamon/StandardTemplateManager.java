package org.modusponens.jtt;

import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class StandardTemplateManager
    implements TemplateManager
{
    public Template getInstance(String p_path, Writer p_writer)
    {
        // this method SHOULD:
        //   use its own class loader (!)
        //   check that the class exists. if it doesn't,
        //     to generate it.
        //   verify that the class is up to date. if it isn't
        //     generate it.
        try
        {
            Class c = Class.forName(p_path + "Impl");
            Constructor con =
                c.getConstructor(new Class [] { Writer.class,
                                                TemplateManager.class });
            return (Template) con.newInstance(new Object [] { p_writer, this });
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
