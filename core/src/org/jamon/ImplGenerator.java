package org.modusponens.jtt;

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
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

    public void caseADefComponent(ADefComponent node)
    {
        handleBody();
        super.caseADefComponent(node);
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
        if (getCurrentDef() != null)
        {
            // FIXME
            System.err.println("in def body");
            return;
        }
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
        handleBody();
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
        String asString()
            throws JttException;
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

    private class CallStatement
        implements Statement
    {
        CallStatement(String p_path,List p_params)
        {
            m_path = p_path;
            m_params = new HashMap();
            for (Iterator p = p_params.iterator(); p.hasNext(); /* */)
            {
                AParam param = (AParam) p.next();
                m_params.put(param.getIdentifier().getText(),
                             param.getParamExpr().getText().trim());
            }
        }
        private final String m_path;
        private final Map m_params;
        private String getAbsolutePath()
        {
            return (m_path.charAt(0) == '/')
                ? m_path
                : "/" + getPackageName().replace('.','/') + '/' + m_path;
        }

        private String getClassName()
        {
            return getAbsolutePath().substring(1).replace('/','.');
        }
        public String asString()
            throws JttException
        {
            StringBuffer s = new StringBuffer();
            s.append("{\n  ");
            s.append(getClassName());
            s.append(" c = (");
            s.append(getClassName());
            s.append(") getTemplateManager().getInstance(\"");
            s.append(getAbsolutePath());
            s.append("\", getWriter());\n");

            List requiredArgs = new ArrayList();
            try
            {
                Class c = Class.forName(getClassName());
                requiredArgs.addAll
                    (Arrays.asList
                     ((String []) c.getField("RENDER_ARGS").get(null)));
            }
            catch (ClassNotFoundException e)
            {
                throw new JttException("Class " + e.getMessage() + " not found",e);
            }
            catch (IllegalAccessException e)
            {
                throw new JttException(e);
            }
            catch (NoSuchFieldException e)
            {
                throw new JttException(e);
            }

            for (Iterator i = m_params.keySet().iterator();
                 i.hasNext();
                 /* */ )
            {
                String name = (String) i.next();
                if (! requiredArgs.contains(name) )
                {
                    s.append("  c.set");
                    s.append(capitalize(name));
                    s.append("(");
                    s.append(m_params.get(name));
                    s.append(");\n");
                }
            }
            s.append("  c.render(");
            for (Iterator i = requiredArgs.iterator(); i.hasNext(); /* */)
            {
                String name = (String) i.next();
                String expr = (String) m_params.get(name);
                if (expr == null)
                {
                    throw new JttException("No parameter supplied for argument " + name + " in call to " + m_path);
                }
                s.append(expr);
                if (i.hasNext())
                {
                    s.append(",");
                }
            }
            s.append(");\n");
            s.append("}\n");
            return s.toString();
        }
    }
}
