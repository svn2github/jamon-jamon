package org.modusponens.jtt;

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class ImplGenerator extends BaseGenerator
{
    private List m_statements = new ArrayList();
    private StringBuffer m_current = new StringBuffer();
    private Set m_calls = new HashSet();

    public ImplGenerator(Writer p_writer,
                         String p_packageName,
                         String p_className)
    {
        super(p_writer,p_packageName,p_className);
    }

    public Iterator getCalledTemplateNames()
    {
        return m_calls.iterator();
    }

    public void generateClassSource()
        throws IOException
    {
        generatePrologue();
        generateImports();
        generateDeclaration();
        generateConstructor();
        generateRender();
        generateOptionalArgs();
        generateEpilogue();
    }

    public void caseABodyComponent(ABodyComponent node)
    {
        m_current.append(node.getText().getText());
    }

    public void caseANewlineComponent(ANewlineComponent node)
    {
        m_current.append(node.getNewline().getText());
    }

    public void caseAJavaComponent(AJavaComponent node)
    {
        handleBody();
        StringBuffer buf = new StringBuffer();
        for (Iterator i = node.getAny().iterator(); i.hasNext(); /* */)
        {
            buf.append(((TAny)i.next()).getText());
        }
        addStatement(new RawStatement(buf.toString()));
    }

    public void caseAJlineComponent(AJlineComponent node)
    {
        handleBody();
        addStatement(new RawStatement(node.getFragment().getText()));
    }

    private void handleBody()
    {
        if (m_current.length() > 0)
        {
            addStatement(new WriteStatement("\""
                                          + javaEscape(m_current.toString())
                                          + "\"",
                                          Encoding.NONE));
            m_current = new StringBuffer();
        }
    }

    public void caseAEmitComponent(AEmitComponent node)
    {
        handleBody();
        StringBuffer expr = new StringBuffer();
        TEscape escape = node.getEscape();
        Encoding encoding = Encoding.DEFAULT;
        if (escape != null)
        {
            char c = escape.getText().trim().charAt(1);
            switch (c)
            {
              case 'h': encoding = Encoding.HTML; break;
              case 'u': encoding = Encoding.URL; break;
              case 'n': encoding = Encoding.NONE; break;
              case 'x': encoding = Encoding.XML; break;
              default:
                throw new RuntimeException("Unknown escape " + c);
            }
        }
        for (Iterator i = node.getAny().iterator(); i.hasNext(); /* */)
        {
            expr.append(((TAny)i.next()).getText());
        }
        addStatement(new WriteStatement(expr.toString(),encoding));
    }

    public void caseACallComponent(ACallComponent node)
    {
        String path = asText(node.getPath());
        m_calls.add(path);
        addStatement(new CallStatement(path,node.getParam()));
    }

    private String asText(PPath node)
    {
        if (node instanceof ASimplePath)
        {
            ASimplePath path = (ASimplePath) node;
            if (path.getSlash() != null)
            {
                return "/" + path.getIdentifier().getText();
            }
            else
            {
                return path.getIdentifier().getText();
            }
        }
        else
        {
            AQualifiedPath path = (AQualifiedPath) node;
            return asText(path.getPath())
                + "/"
                + path.getIdentifier().getText();
        }
    }

    public void caseEOF(EOF node)
    {
        handleBody();
    }

    private String javaEscape(String p_string)
    {
        // assert p_string != null
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < p_string.length(); ++i)
        {
            char c = p_string.charAt(i);
            switch(c)
            {
              case '\n': s.append("\\n"); break;
              case '\t': s.append("\\t"); break;
              case '\"': s.append("\\\""); break;
              default: s.append(c);
            }
        }
        return s.toString();
    }

    private void generateDeclaration()
        throws IOException
    {
        print  ("public class ");
        print  (              getClassName());
        println(                           "Impl");
        print  ("  extends ");
        println(           BASE_TEMPLATE);
        print  ("  implements ");
        println(              getClassName());
        println("{");
    }

    private void generateConstructor()
        throws IOException
    {
        print("  public ");
        print(getClassName());
        println("Impl(java.io.Writer p_writer,");
        print  ("        ");
        print  (         TEMPLATE_MANAGER);
        println(                        " p_templateManager)");
        println("  {");
        println("    super(p_writer, p_templateManager);");
        println("  }");
        println();

    }

    private static final String TEMPLATE_MANAGER =
        TemplateManager.class.getName();

    private static final String BASE_TEMPLATE =
        AbstractTemplate.class.getName();

    private void generateRender()
        throws IOException
    {
        print("  public void render(");
        for (Iterator i = getRequiredArgs(); i.hasNext(); /* */)
        {
            String name = (String) i.next();
            print(getArgType(name));
            print(" ");
            print(name);
            if (i.hasNext())
            {
                print(", ");
            }
        }
        println(")");

        println("    throws java.io.IOException");
        println("  {");
        for (Iterator i = m_statements.iterator(); i.hasNext(); /* */)
        {
            println(((Statement)i.next()).asString());
        }
        println("  }");
    }

    private void generateOptionalArgs()
        throws IOException
    {
        for (Iterator i = getOptionalArgs(); i.hasNext(); /* */)
        {
            println();
            String name = (String) i.next();
            print("  public void set");
            print(capitalize(name));
            print("(");
            String type = getArgType(name);
            print(type);
            print(" p_");
            print(name);
            println(")");
            println("  {");
            print("    ");
            print(name);
            print(" = p_");
            print(name);
            println(";");
            println("  }");
            println();
            print("  private ");
            print(type);
            print(" ");
            print(name);
            print(" = ");
            print(getDefault(name));
            println(";");
        }
    }

    private void generateEpilogue()
        throws IOException
    {
        println();
        println("}");
    }

    private void addStatement(Statement p_statement)
    {
        m_statements.add(p_statement);
    }

    private static class Encoding
    {
        private Encoding(String p_name)
        {
            m_name = p_name;
        }
        public String toString()
        {
            return m_name;
        }
        public boolean equals(Object p_obj)
        {
            return (p_obj instanceof Encoding) ? p_obj == this : false;
        }

        private final String m_name;

        final static Encoding DEFAULT = new Encoding("");
        final static Encoding NONE = new Encoding("Un");
        final static Encoding HTML = new Encoding("Html");
        final static Encoding XML = new Encoding("Xml");
        final static Encoding URL = new Encoding("Url");
    }

    private static interface Statement
    {
        String asString();
    }

    private static class WriteStatement
        implements Statement
    {
        WriteStatement(String p_expr, Encoding p_encoding)
        {
            m_expr = p_expr;
            m_encoding = p_encoding;
        }
        private final String m_expr;
        private final Encoding m_encoding;
        public String asString()
        {
            return "write" + m_encoding + "Escaped(String.valueOf(" + m_expr + "));";
        }
    }

    private static class RawStatement
        implements Statement
    {
        RawStatement(String p_code)
        {
            m_code = p_code;
        }
        private final String m_code;
        public String asString()
        {
            return m_code;
        }
    }

    private static class Param
    {
        Param(String p_name, String p_expr)
        {
            m_name = p_name;
            m_expr = p_expr;
        }
        private final String m_name;
        private final String m_expr;
    }

    private static class CallStatement
        implements Statement
    {
        CallStatement(String p_path,List p_params)
        {
            m_path = p_path;
            m_params = new Param[p_params.size()];
            for (int i = 0; i < m_params.length; ++i)
            {
                AParam p = (AParam) p_params.get(i);
                m_params[i] = new Param(p.getIdentifier().getText(),
                                        p.getParamExpr().getText());
            }
        }
        private final String m_path;
        private final Param [] m_params;
        public String asString()
        {
            return "/* call to " + m_path + " goes here */";
        }
    }
}
