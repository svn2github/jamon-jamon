package org.jamon;

public class JamonException
    extends java.io.IOException
{
    public JamonException(String p_msg)
    {
        this(p_msg,null);
    }

    public JamonException(String p_msg, Throwable p_rootCause)
    {
        super(p_msg);
        m_rootCause = p_rootCause;
    }

    public JamonException(Throwable p_rootCause)
    {
        this(p_rootCause.getMessage(),p_rootCause);
    }

    private final Throwable m_rootCause;

    public Throwable getRootCause()
    {
        return m_rootCause;
    }

    public void printStackTrace(java.io.PrintWriter p_writer)
    {
        if (getRootCause() != null)
        {
            getRootCause().printStackTrace(p_writer);
            p_writer.print("wrapped by ");
        }
        super.printStackTrace(p_writer);
        p_writer.flush();
    }

    public void printStackTrace(java.io.PrintStream p_stream)
    {
        printStackTrace(new java.io.PrintWriter(p_stream));
    }

    public void printStackTrace()
    {
        printStackTrace(System.err);
    }
}
