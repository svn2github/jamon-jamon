package org.jamon.benchmark;

import org.jamon.StandardTemplateManager;
import java.io.IOException;

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

    public void run()
    {
        try
        {
            NullWriter writer = new NullWriter();
            for (int i = 0; i < m_iterations; ++i)
            {
                s_top.writeTo(writer).render();
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