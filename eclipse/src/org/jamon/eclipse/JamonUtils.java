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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2005 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.eclipse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class JamonUtils
{
    private JamonUtils() {}

    private interface LineNumberHandler
    {
        void recordPosition(int p_javaLine, Location p_templateLocation);
    }

    private static class JavaLineListHandler implements LineNumberHandler
    {
        public void recordPosition(int p_javaLine, Location p_templateLocation)
        {
            lineNumbers.add(p_templateLocation.getLine());
        }

        private List<Integer> lineNumbers = new ArrayList<Integer>();
    }

    private static class NearestMatchLineHandler implements LineNumberHandler
    {
        private final Location m_targetLocation;
        private Location m_bestMatchLocation = new Location(0, 0);
        private int m_currentBestMatchJavaLine = 1;

        public NearestMatchLineHandler(Location p_targetLocation)
        {
            m_targetLocation = p_targetLocation;
        }

        public void recordPosition(int p_javaLine, Location p_templateLocation)
        {
            if (p_templateLocation.compareTo(m_targetLocation) <= 0
                && p_templateLocation.compareTo(m_bestMatchLocation) > 0)
            {
                m_currentBestMatchJavaLine = p_javaLine;
                m_bestMatchLocation = p_templateLocation;
            }
        }

        public int getBestMatchLine()
        {
            return m_currentBestMatchJavaLine;
        }
    }

    public static List<Integer> readLineNumberMappings(
        IFile p_generatedJavaFile)
        throws CoreException, IOException
    {
        JavaLineListHandler lineHandler = new JavaLineListHandler();
        processLines(p_generatedJavaFile, lineHandler);
        return lineHandler.lineNumbers;
    }


    /**
     * Get the line number of a generated java file which best corresponds to a
     * location in the original template file.
     * @param p_generatedJavaFile the generated java file
     * @param p_targetLocation the location in the source file
     * @return the best matching line number (0-based)
     * @throws CoreException
     * @throws IOException
     */
    public static int getBestMatchJavaLine(
        IFile p_generatedJavaFile, Location p_targetLocation)
        throws CoreException, IOException
    {
        NearestMatchLineHandler lineHandler =
            new NearestMatchLineHandler(p_targetLocation);
        processLines(p_generatedJavaFile, lineHandler);
        // The line number returned is 1-based; we need to return 0-based.
        // However, we also want to go to the line immediately following the
        // comment line, so just return the line number as is.
        return lineHandler.getBestMatchLine();
    }

    private static void processLines(
        IFile p_generatedJavaFile, LineNumberHandler p_lineNumberHandler)
        throws CoreException, IOException
    {
        Location currentTemplateLocation = new Location(1, 1);
        int javaLineNumber = 1;
        LineNumberReader reader = null;
        try
        {
            reader = new LineNumberReader(new InputStreamReader(
                p_generatedJavaFile.getContents()));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("// "))
                {
                    int commaPosition = trimmedLine.indexOf(", ", 3);
                    if (commaPosition > 0)
                    {
                        try
                        {
                            currentTemplateLocation = new Location(
                                Integer.parseInt(
                                    trimmedLine.substring(3, commaPosition)),
                                Integer.parseInt(
                                    trimmedLine.substring(commaPosition + 2)));
                        }
                        catch (NumberFormatException e)
                        {
                            JamonProjectPlugin.getDefault().logInfo(
                                "parsing " + trimmedLine + ": " + e.getMessage());
                        }
                    }
                }
                p_lineNumberHandler.recordPosition(javaLineNumber++,
                                                   currentTemplateLocation);
            }
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

}
