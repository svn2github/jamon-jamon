package org.modusponens.jtt;

import java.util.*;
import org.modusponens.jtt.node.*;
import org.modusponens.jtt.analysis.*;

public class PrettyPrinterAdapter extends AnalysisAdapter
{
    public void caseAArg(AArg arg)
    {
        System.out.print("  ");
        arg.getType().apply(this);
        System.out.print(" ");
        arg.getName().apply(this);
        if (arg.getDefault() != null)
        {
            System.out.println(" => " +
                               ((ADefault) arg.getDefault()).getFragment().getText());
        }
        else
        {
            System.out.println();
        }
    }

    public void caseAParam(AParam param)
    {
        param.getName().apply(this);
        System.out.print(" => ");
        System.out.println(param.getParamExpr().getText());
    }

    public void caseACallComponent(ACallComponent call)
    {
        System.out.print("<& ");
        call.getName().apply(this);
        for (Iterator i = call.getParam().iterator(); i.hasNext(); /* */)
        {
            System.out.print(", ");
            ((Node)i.next()).apply(this);
        }
        System.out.print(" &>");
    }

    public void caseAImportsComponent(AImportsComponent imports)
    {
        System.out.println("<%import>");
        for (Iterator i = imports.getName().iterator(); i.hasNext(); /* */ )
        {
            System.out.print("  ");
            ((PName)i.next()).apply(this);
            System.out.println();
        }
        System.out.println("</%import>");
    }

    public void caseAJlineComponent(AJlineComponent jline)
    {
        System.out.print("% ");
        System.out.println(jline.getFragment().getText());
    }

    public void caseAJavaComponent(AJavaComponent java)
    {
        System.out.print("<%java>");
        for (Iterator i = java.getAny().iterator(); i.hasNext(); /* */ )
        {
            System.out.print(((TAny)i.next()).getText());
        }
        System.out.print("</%java>");
    }

    public void caseAEmitComponent(AEmitComponent emit)
    {
        System.out.print("<% ");
        for (Iterator i = emit.getAny().iterator(); i.hasNext(); /* */ )
        {
            System.out.print(((TAny)i.next()).getText());
        }
        System.out.print(" %>");
    }

    public void caseASimpleName(ASimpleName name)
    {
        name.getIdentifier().apply(this);
    }

    public void caseTIdentifier(TIdentifier id)
    {
        System.out.print(id.getText());
    }

    public void caseAQualifiedName(AQualifiedName name)
    {
        name.getName().apply(this);
        System.out.print('.');
        name.getIdentifier().apply(this);
    }

    public void caseAType(AType type)
    {
        type.getName().apply(this);
        for (Iterator i = type.getBrackets().iterator(); i.hasNext(); i.next())
        {
            System.out.print("[]");
        }
    }

    public void caseStart(Start start)
    {
        start.getPTemplate().apply(this);
    }

    public void caseATemplate(ATemplate node)
    {
        for (Iterator i = node.getComponent().iterator(); i.hasNext(); /**/ )
        {
            ((Node)i.next()).apply(this);
        }
    }

    public void caseAArgsComponent(AArgsComponent args)
    {
        System.out.println("<%args>");
        for (Iterator i = args.getArg().iterator(); i.hasNext(); /**/ )
        {
            ((Node)i.next()).apply(this);
        }
        System.out.println("</%args>");
    }

    public void caseABodyComponent(ABodyComponent body)
    {
        System.out.print(body.getText().getText());
    }

    public void caseANewlineComponent(ANewlineComponent newline)
    {
        System.out.print(newline.getNewline().getText());
    }

    public void defaultCase(Node node)
    {
        System.out.println(node.getClass().getName());
    }

}
