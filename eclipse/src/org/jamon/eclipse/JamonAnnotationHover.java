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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;

public class JamonAnnotationHover implements IAnnotationHover
{
    protected List getAnnotationsForLine(ISourceViewer p_viewer, int p_line) 
    {
        //FIXME - should we only be looking at jamon-generated annotations?
        IDocument document= p_viewer.getDocument();
        IAnnotationModel model= p_viewer.getAnnotationModel();
        
        if (model == null)
            return null;
        
        List exact = new ArrayList();
        
        for (Iterator i = model.getAnnotationIterator(); i.hasNext(); )
        {
            Annotation annotation = (Annotation) i.next();
            try
            {
                if (annotation.getText() != null 
                    && document.getLineOfOffset(
                        model.getPosition(annotation).getOffset()) ==  p_line)
                {
                    exact.add(annotation);
                }
            }
            catch (BadLocationException e)
            {
                //FIXME - really ok to ignore this?
            }
        }
        return exact;
    }

    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) 
    {
        List annotations = getAnnotationsForLine(sourceViewer, lineNumber);
        if (annotations != null)
        {
            StringBuffer messages = new StringBuffer();
            boolean printedLines = false;
            for (Iterator i = annotations.iterator(); i.hasNext(); )
            {
                Annotation annotation = (Annotation) i.next();
                String message= annotation.getText();
                if (message != null && message.trim().length() > 0)
                {
                    if (printedLines)
                    {
                        messages.append("\n");
                    }
                    messages.append(message.trim());
                    printedLines = true;
                }
            }
            return messages.toString();
        }
        return null;
    }
}
