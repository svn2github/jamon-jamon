package org.jamon.benchmark;

import org.jamon.StandardTemplateManager;
import java.io.IOException;
import java.io.StringWriter;

public class Benchmark
    implements Runnable
{
    private Benchmark(int p_iterations)
    {
        m_iterations = p_iterations;
    }

    private static StandardTemplateManager s_manager;
    private static Top s_top;
    private static int s_started;
    private static String s_expected;

    private static void verify(String p_string, String p_prefix)
    {
        String ex = p_prefix + s_expected;
        if( ! ex.equals( p_string ) )
        {
            System.err.println("Expected " + ex + "\ngot " + p_string);
            System.exit(4);
        }
    }

    public void run()
    {
        try
        {
            StringWriter writer = new StringWriter();
            s_top.writeTo(writer).render();
            verify(writer.toString(),"");
            for (int i = 1; i <= m_iterations; ++i)
            {
                writer = new StringWriter();
                s_top.writeTo(writer).setK(i);
                s_top.render();
                verify(writer.toString(),i + "");
            }
            synchronized (Benchmark.class)
            {
                s_started--;
                Benchmark.class.notifyAll();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private final int m_iterations;

    public static void main(String[] args)
    {
        try
        {
            s_manager =
                new StandardTemplateManager(new StandardTemplateManager.Data()
                                            .setDynamicRecompilation(false));
            s_top = new Top(s_manager);
            StringWriter w = new StringWriter();
            s_top.writeTo(w).render();
            s_expected = w.toString();
            int i = s_expected.indexOf('\n');
            s_expected = s_expected.substring(i);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(2);
        }

        long start = System.currentTimeMillis();
        new Benchmark(1000).run();
        long end = System.currentTimeMillis();
        System.out.println("Single thread total time: " + (end-start) + "ms");

        Thread[] threads = new Thread[20];
        for (int i = 0; i < threads.length; ++i)
        {
            threads[i] = new Thread(new Benchmark(50));
        }
        s_started = threads.length;
        start = System.currentTimeMillis();
        for (int i = 0; i < threads.length; ++i)
        {
            threads[i].start();
        }
        while (s_started > 0)
        {
            synchronized (Benchmark.class)
            {
                try
                {
                    Benchmark.class.wait();
                }
                catch (InterruptedException e)
                {
                }
            }
        }
        end = System.currentTimeMillis();
        System.out.println("Multi thread total time: " + (end-start) + "ms");

    }
}