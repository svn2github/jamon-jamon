package org.jamon.benchmark;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jamon.TemplateLocation;
import org.jamon.TemplateSource;
import org.jamon.FileTemplateSource;
import org.jamon.codegen.TemplateDescriber;
import org.jamon.emit.EmitMode;
import org.jamon.node.AbstractNode;
import org.jamon.node.Analysis;
import org.jamon.node.AnalysisAdapter;
import org.jamon.node.DepthFirstAnalysisAdapter;
import org.jamon.node.TopNode;

public class ParserBenchmarks
{
    private static final int ITERATIONS = 100;

    private static long benchmarkAnalysisAdapter(TopNode p_start)
    {
        Analysis adapter = new AnalysisAdapter();
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++)
        {
            for (AbstractNode subNode : p_start.getSubNodes())
            {
                subNode.apply(adapter);
            }
        }
        return System.currentTimeMillis() - start;
    }

    private static long benchmarkDepthFirstAdapter(TopNode p_start)
    {
        Analysis adapter = new DepthFirstAnalysisAdapter();
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++)
        {
            p_start.apply(adapter);
        }
        return System.currentTimeMillis() - start;
    }

    public static class StringTemplateSource implements TemplateSource
    {
        public StringTemplateSource(File p_file) throws IOException
        {
            InputStream input = null;
            try
            {
                input = new FileInputStream(p_file);
                final int bufferSize = 102400;;
                byte[] buffer = new byte[bufferSize];
                m_contents = new byte[input.read(buffer, 0, bufferSize)];
                System.arraycopy(buffer, 0, m_contents, 0, m_contents.length);
            }
            finally
            {
                if (input != null)
                {
                    input.close();
                }
            }
        }

        public long lastModified(String p_templatePath)
        {
            return 0;
        }

        public boolean available(String p_templatePath)
        {
            return true;
        }

        public InputStream getStreamFor(String p_templatePath)
        {
            return new ByteArrayInputStream(m_contents);
        }

        public String getExternalIdentifier(String p_templatePath)
        {
            return p_templatePath;
        }

        final byte[] m_contents;

        public TemplateLocation getTemplateLocation(String p_templatePath)
        {
            return null;
        }

        public Properties getProperties() throws IOException
        {
            return new Properties();
        }
    }

    private static long benchmarkParser(String p_fileName) throws IOException
    {
        TemplateDescriber describer = new TemplateDescriber(
            new StringTemplateSource(new File(p_fileName)),
            null,
            EmitMode.STANDARD);
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++)
        {
            describer.parseTemplate("foo");
        }
        return System.currentTimeMillis() - start;
    }


    public static void main(String[] args) throws Exception
    {
        if (args.length != 3)
        {
            throw new IllegalArgumentException(
                "usage: java org.jamon.SableCCBenchmarks templates-dir template-file sample-file");
        }

        System.out.println("testing for " + ITERATIONS + " iterations");
        String templatesDir = args[0];
        String template = args[1];
        String sampleFileName = args[2];

        TemplateSource templateSource = new FileTemplateSource(templatesDir);
        TemplateDescriber templateDescriber =
            new TemplateDescriber(templateSource, null, EmitMode.STANDARD);

        TopNode start = templateDescriber.parseTemplate(template);
        System.out.println("AnalysisAdapter total time: " +
                           + benchmarkAnalysisAdapter(start)
                           + "ms");
        System.out.println("DepthFirstAdapter total time: " +
                           + benchmarkDepthFirstAdapter(start)
                           + "ms");
        System.out.println("Parser total time: " +
            + benchmarkParser(sampleFileName)
            + "ms");
    }
}
