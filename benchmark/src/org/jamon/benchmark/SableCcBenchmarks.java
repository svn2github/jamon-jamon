package org.jamon.benchmark;

import java.util.Iterator;

import org.jamon.TemplateSource;
import org.jamon.FileTemplateSource;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.codegen.Analyzer;
import org.jamon.node.*;

import org.jamon.analysis.AnalysisAdapter;
import org.jamon.analysis.DepthFirstAdapter;

public class SableCcBenchmarks
{
    private static final int ITERATIONS = 100;

    private static long benchmarkAnalysisAdapter(Start p_start)
    {
        AnalysisAdapter adapter = new AnalysisAdapter();
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++)
        {
            for (Iterator comp = ((ATemplate) p_start.getPTemplate())
                     .getComponent().iterator();
                 comp.hasNext(); )
            {
                ((PComponent) comp.next()).apply(adapter);
            }
        }
        return System.currentTimeMillis() - start;
    }

    private static long benchmarkDepthFirstAdapter(Start p_start)
    {
        AnalysisAdapter adapter = new DepthFirstAdapter();
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++)
        {
            p_start.apply(adapter);
        }
        return System.currentTimeMillis() - start;
    }


    public static void main(String[] args) throws Exception
    {
        if (args.length != 2)
        {
            throw new IllegalArgumentException(
                "usage: java org.jamon.SableCCBenchmarks templates-dir template-file");
        }

        System.out.println("testing for " + ITERATIONS + " iterations");
        String templatesDir = args[0];
        String template = args[1];

        TemplateSource templateSource = new FileTemplateSource(templatesDir);
        TemplateDescriber templateDescriber =
            new TemplateDescriber(templateSource, null);

        Start start = templateDescriber.parseTemplate(template);
        System.out.println("AnalysisAdapter total time: " +
                           + benchmarkAnalysisAdapter(start)
                           + "ms");
        System.out.println("DepthFirstAdapter total time: " +
                           + benchmarkDepthFirstAdapter(start)
                           + "ms");
    }
}
