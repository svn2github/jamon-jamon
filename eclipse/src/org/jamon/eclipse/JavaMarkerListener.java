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
import java.util.Collections;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author ian
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JavaMarkerListener implements IResourceChangeListener
{
    public JavaMarkerListener(final IFolder p_templateFolder,
                              final IFolder p_generatedSourcesFolder)
    {
        m_templateFolder = p_templateFolder;
        m_generatedSourcesFolder = p_generatedSourcesFolder;
    }
    
    public class ResourceDeltaVisitor implements IResourceDeltaVisitor
    {
        public boolean visit(IResourceDelta p_delta) throws CoreException
        {
            if (!m_generatedSourcesFolder.getFullPath()
                    .isPrefixOf(p_delta.getFullPath())
                && !p_delta.getFullPath()
                    .isPrefixOf(m_generatedSourcesFolder.getFullPath()))
            {
                return false;
            }
            if (p_delta.getResource().getType() == IResource.FILE
                && "java".equals(p_delta.getFullPath().getFileExtension()))
            {
                GeneratedResource generatedResource = 
                    new GeneratedResource((IFile) p_delta.getResource());
                generatedResource.getTemplateFile().deleteMarkers(
                    javaMarkerId, true, IResource.DEPTH_ZERO);
                
                IMarkerDelta[] markerDeltas = p_delta.getMarkerDeltas();
                for (int i = 0; i < markerDeltas.length; i++)
                {
                    switch(markerDeltas[i].getKind())
                    {
                    case IResourceDelta.ADDED:
                    case IResourceDelta.CHANGED:
                        copyMarker(generatedResource, 
                                   markerDeltas[i].getMarker());
                    break;
                    }
                }
            }
            return true;
        }

        private void copyMarker(GeneratedResource p_generatedResource, 
                                IMarker p_marker) throws CoreException
        {
            if (p_marker.isSubtypeOf(IMarker.PROBLEM))
            {
                String message = p_marker.getAttribute(IMarker.MESSAGE, null);
                if (message != null && message.length() > 0)
                {
                    EclipseUtils.populateProblemMarker(
                        p_generatedResource
                            .getTemplateFile()
                            .createMarker(javaMarkerId), 
                        p_generatedResource.getTemplateLineNumber(
                            p_marker.getAttribute(IMarker.LINE_NUMBER, 1)),
                        message);
                }
            }
        }
        
        private class GeneratedResource
        {
            public GeneratedResource(IFile p_generatedJavaFile)
                throws CoreException
            {
                IPath path = p_generatedJavaFile.getFullPath();
                String className = path.removeFileExtension().lastSegment();
                if (className.endsWith("Impl"))
                {
                    className = className.substring(0, className.length() - 4);
                    m_isImpl = true;
                }
                else
                {
                    m_isImpl = false;
                }
                m_templateFile = m_templateFolder.getFile(
                    path
                        .removeFirstSegments(
                            m_generatedSourcesFolder.getFullPath().segmentCount())
                        .removeLastSegments(1)
                        .append(className)
                        .addFileExtension(JamonNature.JAMON_EXTENSION));
                
                try
                {
                    m_locations = readLineNumberMappings(p_generatedJavaFile);
                }
                catch (IOException e)
                {
                    m_locations = Collections.EMPTY_LIST;
                    JamonProjectPlugin.getDefault().logError(e);
                }
            }
            
            private List readLineNumberMappings(IFile p_generatedJavaFile)
                throws CoreException, IOException
            {
                List lineNumbers = new ArrayList();
                
                Integer currentTemplateLineNumber = new Integer(1);
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
                            int commaPosition = trimmedLine.indexOf(',', 3);
                            if (commaPosition > 0)
                            {
                                try
                                {
                                    currentTemplateLineNumber = new Integer(
                                       trimmedLine.substring(3, commaPosition));
                                }
                                catch (NumberFormatException e)
                                {}
                            }
                        }
                        lineNumbers.add(currentTemplateLineNumber);
                    }
                }
                finally
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                }
                return lineNumbers;
            }

            public int getTemplateLineNumber(int p_javaLineNumber)
            {
                if (m_locations.isEmpty())
                {
                    return 1;
                }
                else
                {
                    return ((Integer) m_locations.get(
                        Math.min(p_javaLineNumber, m_locations.size() - 1)))
                        .intValue();
                }
            }
            
            public boolean isImpl()
            {
                return m_isImpl;
            }
            
            public IFile getTemplateFile()
            {
                return m_templateFile;
            }
            
            private final IFile m_templateFile;
            private final boolean m_isImpl;
            private List m_locations;
        }
    }
    
    public void resourceChanged(IResourceChangeEvent p_event)
    {
        try
        {
            p_event.getDelta().accept(new ResourceDeltaVisitor());
        }
        catch (CoreException e)
        {
            JamonProjectPlugin.getDefault().logError(e);
        }
    }
    
    private final IFolder m_templateFolder, m_generatedSourcesFolder;
    private final String javaMarkerId = 
        JamonProjectPlugin.getDefault().pluginId() + ".javaMarker";
}
