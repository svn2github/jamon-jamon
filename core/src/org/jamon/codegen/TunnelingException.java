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
 * Contributor(s): Ian Robertson
 */

package org.jamon.codegen;

import org.jamon.node.Token;

public class TunnelingException
    extends RuntimeException
{
    TunnelingException(String p_message)
    {
        this(p_message, null, null);
    }

    private TunnelingException(String p_msg,
                               Throwable p_rootCause,
                               Token p_token)
    {
        super(p_msg);
        m_rootCause = p_rootCause;
        m_token = p_token;
    }

    public TunnelingException(Throwable p_rootCause)
    {
        this(p_rootCause.getMessage(), p_rootCause, null);
    }

    public TunnelingException(String p_message, Token p_token)
    {
        this(p_message, null, p_token);
    }

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

    Token getToken()
    {
        return m_token;
    }

    private final Token m_token;
    private final Throwable m_rootCause;
}
