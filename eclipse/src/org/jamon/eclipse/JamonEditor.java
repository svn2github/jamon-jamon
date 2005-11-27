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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

public class JamonEditor extends AbstractDecoratedTextEditor
{
    private static final String EDITOR_CONTEXT_MENU_ID =
        JamonProjectPlugin.getDefault().pluginId() + ".editorContext";

    public JamonEditor()
    {
        setEditorContextMenuId(EDITOR_CONTEXT_MENU_ID);
    }
    @Override
    protected void initializeEditor()
    {
        super.initializeEditor();
        setSourceViewerConfiguration(
            new JamonEditorSourceViewerConfiguration());
    }

    public synchronized TemplateResources getTemplateResources()
        throws CoreException
    {
        if (m_templateResources == null)
        {
            IEditorInput editorInput = getEditorInput();
            if (editorInput instanceof IPathEditorInput)
            {
                IPath path = ((IPathEditorInput)editorInput).getPath();
                IFile[] files = ResourcesPlugin
                    .getWorkspace()
                    .getRoot()
                    .findFilesForLocation(path);
                if (files.length > 0)
                {
                    IProject project = files[0].getProject();
                    JamonNature nature =
                        (JamonNature) project.getNature(JamonNature.natureId());
                    m_templateResources = new TemplateResources(
                        files[0],
                        nature.getTemplateOutputFolder(),
                        nature.getTemplateSourceFolder());
                }
            }
        }
        return m_templateResources;
    }

    public Location getCursorLocation() throws BadLocationException
    {
        ISourceViewer sourceViewer = getSourceViewer();
        int caret = widgetOffset2ModelOffset(
            sourceViewer, sourceViewer.getTextWidget().getCaretOffset());
        IDocument document= sourceViewer.getDocument();

        int line = document.getLineOfOffset(caret);

        int lineOffset = document.getLineOffset(line);
        int column = caret - lineOffset;
        return new Location(line + 1, column + 1);
    }

    private TemplateResources m_templateResources;
}
