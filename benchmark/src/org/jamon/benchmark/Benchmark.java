package org.jamon.benchmark;

import org.jamon.StandardTemplateManager;
import org.jamon.TemplateManagerSource;
import java.io.IOException;
import java.io.StringWriter;

public class Benchmark
{
    private static class Runner
        implements Runnable
    {
        private Runner(int p_iterations)
        {
            m_iterations = p_iterations;
        }

        private void verify(String p_string, String p_prefix)
        {
            String ex = p_prefix + s_expected;
            if( ! ex.equals( p_string ) )
            {
                System.err.println("Expected " + ex + "\ngot " + p_string);
                System.exit(4);
            }
        }

        private void renderAndVerify(Integer p_k)
            throws IOException
        {
            verify(renderTop(p_k), p_k == null ? "" : p_k.toString());
        }

        public void run()
        {
            try
            {
                renderAndVerify(null);
                for (int k = 1; k <= m_iterations; ++k)
                {
                    renderAndVerify(new Integer(k));
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
    }

    private static int s_started;
    private static String s_expected;

    private static final int ITERATIONS = 1000;
    private static final int THREADS = 20;

    private static String renderTop(Integer p_k)
        throws IOException
    {
        Top top = new Top();
        if (p_k != null)
        {
            top.setK(p_k.intValue());
        }
        StringWriter writer = new StringWriter();
        top.render(writer);
        return writer.toString();
    }

    private static void setup()
        throws IOException
    {
        TemplateManagerSource.setTemplateManager
            (new StandardTemplateManager
             (new StandardTemplateManager.Data()
              .setDynamicRecompilation(false)));
        s_expected = renderTop(null);
        int i = s_expected.indexOf('\n');
        s_expected = s_expected.substring(i);
    }


    private static long benchmarkSingleThreaded()
    {
        long start = System.currentTimeMillis();
        new Runner(ITERATIONS).run();
        return System.currentTimeMillis() - start;
    }

    private static long benchmarkMultiThreaded()
    {
        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < threads.length; ++i)
        {
            threads[i] = new Thread(new Runner(ITERATIONS / THREADS));
        }
        s_started = threads.length;

        long start = System.currentTimeMillis();
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
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args)
    {
        try
        {
            setup();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(2);
        }

        System.out.println("Single thread total time: " +
                           + benchmarkSingleThreaded()
                           + "ms");
        System.out.println("Multi thread total time: "
                           + benchmarkMultiThreaded()
                           + "ms");
    }
}
