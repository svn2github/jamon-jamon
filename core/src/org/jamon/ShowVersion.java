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
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.text.DecimalFormat;

/**
 * Class used during release for obtaining version information.
 */

public class ShowVersion
{
    private static void usage()
    {
        System.err.println( "Usage: "
                            + ShowVersion.class.getName()
                            + " [ " + CMD_SHOW
                            + " | " + CMD_NEXT_MINOR
                            + " | " + CMD_NEXT_MAJOR
                            + " | " + CMD_MAJOR
                            + " | " + CMD_MINOR
                            + " ]" );
        System.exit(1);
    }

    private static final String CMD_SHOW = "show";
    private static final String CMD_NEXT_MAJOR = "nextmajor";
    private static final String CMD_NEXT_MINOR = "nextminor";
    private static final String CMD_MINOR = "minor";
    private static final String CMD_MAJOR = "major";

    private static final DecimalFormat MINOR_FORMAT =
        new DecimalFormat("00");

    private static final DecimalFormat MAJOR_FORMAT =
        new DecimalFormat("#");

    /**
     * Show version info.
     * <ul>
     *   <li><b>current</b> display current version info</li>
     *   <li><b>next</b> display next version number</li>
     *   <li><b>major</b> display next major version number</li>
     *   <li><b>show</b> display full current version information</li>
     * </ul>
     * Default is <b>show</b>.
     */
    public static void main(String [] args)
    {
        try
        {
            if (args.length > 2)
            {
                usage();
            }
            String cmd = args.length == 1 ? args[0] : CMD_SHOW;
            ResourceBundle resources =
                ResourceBundle.getBundle("org.jamon.Resources");

            int major = Integer.parseInt(resources.getString("version.major"));
            int minor = Integer.parseInt(resources.getString("version.minor"));
            if ( cmd.equals(CMD_SHOW) )
            {
                boolean isDev =
                    ! "true".equalsIgnoreCase(resources.getString("version.release"));
                System.out.println( "Jamon version "
                                    + MAJOR_FORMAT.format(major)
                                    + "."
                                    + MINOR_FORMAT.format(minor)
                                    + ( isDev ? "dev" : "")
                                    + " ("
                                    + resources.getString("cvsversion")
                                    + ")" );
            }
            else if ( cmd.equals(CMD_MAJOR) )
            {
                System.out.println( MAJOR_FORMAT.format(major) );
            }
            else if ( cmd.equals(CMD_MINOR) )
            {
                System.out.println( MINOR_FORMAT.format(minor) );
            }
            else if ( cmd.equals(CMD_NEXT_MAJOR) )
            {
                System.out.println( MAJOR_FORMAT.format(major+1) );
            }
            else if ( cmd.equals(CMD_NEXT_MINOR) )
            {
                System.out.println( MINOR_FORMAT.format(minor+1) );
            }
            else
            {
                usage();
            }
        }
        catch (MissingResourceException e)
        {
            System.err.println( "Unable to determine version: " );
            e.printStackTrace();
        }
    }

    /**
     * This class is not instantiable.
     */
    private ShowVersion()
    {
    }
}
