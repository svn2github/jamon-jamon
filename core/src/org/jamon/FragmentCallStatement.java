package org.jamon;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class FragmentCallStatement
    extends CallStatement
{
    FragmentCallStatement(String p_path, Map p_params, List p_fragment)
    {
        super(p_path, p_params);
        m_fragment = p_fragment;
    }

    private final List m_fragment;

    private final static String IOEXCEPTION_CLASS =
        IOException.class.getName();

    private Object getFargName(ImplAnalyzer p_analyzer,
                               TemplateDescriber p_describer)
        throws JamonException
    {
        if (isDefCall(p_analyzer))
        {
            return p_analyzer.getFargNames(getPath()).next();
        }
        else
        {
            return p_describer
                .getFargNames(p_analyzer.getAbsolutePath(getPath())).next();
        }
    }



    public void generateSource(PrintWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
        throws IOException
    {
        String fragVar = p_analyzer.newVarName();

        String fargName = (String) getFargName(p_analyzer, p_describer);
        FargInfo fargInfo =
            isDefCall(p_analyzer)
            ? p_analyzer.getFargInfo(fargName)
            : p_describer.getFargInfo(p_analyzer.getAbsolutePath(getPath()),fargName);

        p_writer.println("{");
        p_writer.print("    final ");

        String fargIntf = fargInfo.getFargInterfaceName();
        if (! isDefCall(p_analyzer))
        {
            fargIntf =
                p_resolver.getFullyQualifiedIntfClassName
                    (p_analyzer.getAbsolutePath(getPath()))
                + "." + fargIntf;
        }

        String className = fargInfo.getFargInterfaceName()
            + p_analyzer.newVarName();
        p_writer.print  ("class ");
        p_writer.println(className);
        p_writer.println("  extends org.jamon.AbstractTemplateImpl");
        p_writer.print  ("  implements ");
        p_writer.println(fargIntf);
        p_writer.println("{");
        p_writer.print  ("  ");
        p_writer.print  (className);
        p_writer.println("(org.jamon.TemplateManager p_manager) {");
        p_writer.println("    super(p_manager,\"\");");
        p_writer.println("  }");
        p_writer.print("       public void render(");
        for (Iterator a = fargInfo.getArgumentNames(); a.hasNext(); /* */)
        {
            String arg = (String) a.next();
            p_writer.print(fargInfo.getArgumentType(arg));
            p_writer.print(" ");
            p_writer.print(arg);
            if (a.hasNext())
            {
                p_writer.print(", ");
            }
        }
        p_writer.print(") throws ");
        p_writer.print(IOEXCEPTION_CLASS);
        p_writer.println(" {");
        for (Iterator i = m_fragment.iterator(); i.hasNext(); /* */)
        {
            ((Statement)i.next()).generateSource(p_writer,
                                                 p_resolver,
                                                 p_describer,
                                                 p_analyzer);
            p_writer.println();
        }
        p_writer.println("    }");
        p_writer.println("  }");

        p_writer.print(className);
        p_writer.print(" ");
        p_writer.print(fragVar);
        p_writer.println(" =");
        p_writer.print("      new ");
        p_writer.print(className);
        p_writer.println(" (this.getTemplateManager());");
        p_writer.print(fragVar);
        p_writer.println(".writeTo(this.getWriter());");
        m_params.put(fargName, fragVar);

        super.generateSource(p_writer,p_resolver,p_describer,p_analyzer);

        p_writer.println("}");
    }
}
