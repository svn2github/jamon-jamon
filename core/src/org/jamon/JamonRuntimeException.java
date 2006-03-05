/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon;

public class JamonRuntimeException
    extends RuntimeException
{
    public JamonRuntimeException(String p_msg)
    {
        this(p_msg,null);
    }

    public JamonRuntimeException(String p_msg, Throwable p_rootCause)
    {
        super(p_msg, p_rootCause);
    }

    public JamonRuntimeException(Throwable p_rootCause)
    {
        this(p_rootCause.getMessage(),p_rootCause);
    }

    /**
     * @return the root cause for this exception
     * @deprecated use {@link #getCause} instead
     */
    @Deprecated
    public Throwable getRootCause()
    {
        return getCause();
    }

    @Override public void printStackTrace(java.io.PrintWriter p_writer)
    {
        if (getRootCause() != null)
        {
            getRootCause().printStackTrace(p_writer);
            p_writer.print("wrapped by ");
        }
        super.printStackTrace(p_writer);
        p_writer.flush();
    }

    @Override public void printStackTrace(java.io.PrintStream p_stream)
    {
        printStackTrace(new java.io.PrintWriter(p_stream));
    }

    @Override public void printStackTrace()
    {
        printStackTrace(System.err);
    }
}
