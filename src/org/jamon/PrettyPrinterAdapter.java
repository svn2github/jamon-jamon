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
                               ((ADefault) arg.getDefault()).getFragment());
        }
        else
        {
            System.out.println();
        }
    }

    public void caseASimpleName(ASimpleName name)
    {
        name.getIdentifier().apply(this);
    }

    public void caseTIdentifier(TIdentifier id)
    {
        System.out.print(id);
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
        System.out.print(body);
    }

    public void defaultCase(Node node)
    {
        System.out.println(node.getClass().getName());
    }

}
