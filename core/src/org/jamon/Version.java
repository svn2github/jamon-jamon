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

import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * Class used during release for obtaining version information.
 */

public class Version
{
    private static void usage()
    {
        System.err.println( "Usage: "
                            + Version.class.getName()
                            + " [ " + CMD_SHOW
                            + " | " + CMD_NEXT_MINOR
                            + " | " + CMD_NEXT_MAJOR
                            + " | " + CMD_NEXT_SUB
                            + " | " + CMD_CURRENT
                            + " ]" );
        System.exit(1);
    }

    private static final String CMD_SHOW = "show";
    private static final String CMD_CURRENT = "current";
    private static final String CMD_NEXT_MAJOR = "nextmajor";
    private static final String CMD_NEXT_MINOR = "nextminor";
    private static final String CMD_NEXT_SUB = "nextsub";
    private static final String CMD_CVSTAG = "cvstag";

    private static final ResourceBundle s_resources =
        ResourceBundle.getBundle("org.jamon.Resources");

    public static Version CURRENT =
        new Version(s_resources.getString("org.jamon.version"));

    public static void main(String [] args)
    {
        if (args.length > 2)
        {
            usage();
        }
        String cmd = args.length == 1 ? args[0] : CMD_SHOW;

        if ( cmd.equals(CMD_SHOW) )
        {
            boolean isDev =
                ! "true".equalsIgnoreCase(s_resources.getString
                                          ("org.jamon.version.release"));
            System.out.println( "Jamon version "
                                + CURRENT
                                + ( isDev ? "dev" : "")
                                + " ("
                                + s_resources.getString
                                    ("org.jamon.version.cvs")
                                + ")" );
        }
        else if (cmd.equals(CMD_CVSTAG))
        {
            System.out.println(CURRENT.asCvsTag());
        }
        else if (cmd.equals(CMD_CURRENT))
        {
            System.out.println(CURRENT);
        }
        else if (cmd.equals(CMD_NEXT_MAJOR))
        {
            System.out.println(CURRENT.bumpmajor());
        }
        else if ( cmd.equals(CMD_NEXT_MINOR) )
        {
            System.out.println(CURRENT.bumpminor());
        }
        else if ( cmd.equals(CMD_NEXT_SUB) )
        {
            System.out.println(CURRENT.bumpsub());
        }
        else
        {
            usage();
        }
    }

    private Version(String p_current)
    {
        StringTokenizer tokenizer = new StringTokenizer(p_current,".");
        m_major = Integer.parseInt(tokenizer.nextToken());
        m_minor = Integer.parseInt(tokenizer.nextToken());
        m_sub = tokenizer.hasMoreTokens()
            ? Integer.parseInt(tokenizer.nextToken())
            : 0;
    }

    private Version(int p_major, int p_minor, int p_sub)
    {
        m_major = p_major;
        m_minor = p_minor;
        m_sub = p_sub;
    }

    private final int m_major;
    private final int m_minor;
    private final int m_sub;

    public Version bumpmajor()
    {
        return new Version(m_major+1, 0, 0);
    }

    public Version bumpminor()
    {
        return new Version(m_major, m_minor+1, 0);
    }

    public Version bumpsub()
    {
        return new Version(m_major, m_minor, m_sub+1);
    }

    public String toString()
    {
        return makeString(".");
    }

    private String makeString(String p_delim)
    {
        return m_major
            + p_delim
            + m_minor
            + (m_sub == 0 ? "" : (p_delim + m_sub));
    }

    public String asCvsTag()
    {
        return makeString("_");
    }
}
