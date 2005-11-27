package org.jamon.eclipse.popup;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.jamon.eclipse.EclipseUtils;
import org.jamon.eclipse.JamonEditor;
import org.jamon.eclipse.JamonUtils;
import org.jamon.eclipse.Location;
import org.jamon.eclipse.TemplateResources;

public class SwitchToImplAction implements IEditorActionDelegate
{
    private IEditorPart m_editor;

    public SwitchToImplAction()
    {
        EclipseUtils.logInfo("constructor for SwitchToImpl");
    }
    public void setActiveEditor(IAction p_action, IEditorPart p_targetEditor)
    {
        m_editor = p_targetEditor;
        EclipseUtils.logInfo("setting active editor for SwitchToImpl");
    }

    public void run(IAction p_action)
    {
        if (m_editor instanceof JamonEditor)
        {
            JamonEditor jamonEditor = (JamonEditor)m_editor;
            Location cursorLocation;
            try
            {
                cursorLocation = jamonEditor.getCursorLocation();
                EclipseUtils.logInfo("cursor is at: " + cursorLocation.getLine()
                        + " : " + cursorLocation.getColumn());
                TemplateResources  templateResources =
                    jamonEditor.getTemplateResources();
                IEditorPart editorPart = IDE.openEditor(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
                    templateResources.getImpl());

                if (editorPart instanceof AbstractTextEditor)
                {
                    AbstractTextEditor textEditor = (AbstractTextEditor)editorPart;
                    IDocument document = textEditor.getDocumentProvider()
                        .getDocument(textEditor.getEditorInput());
                    int offset = document.getLineOffset(
                        JamonUtils.getBestMatchJavaLine(
                            templateResources.getImpl(), cursorLocation));
                    textEditor.selectAndReveal(offset, 0);
                }


            }
            catch (BadLocationException e)
            {
                EclipseUtils.logError(e);
            }
            catch (CoreException e)
            {
                EclipseUtils.logError(e);
            }
            catch (IOException e)
            {
                EclipseUtils.logError(e);
            }
        }

    }

    public void selectionChanged(IAction p_action, ISelection p_selection)
    {
        EclipseUtils.logInfo("selectionChanged for SwitchToImpl");
        // TODO Auto-generated method stub

    }
}
