/***********************************************
 * Copyright (c) 2014 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.eclipse.j8marker;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;


public class BaseHandler extends AbstractHandler
{
    static final String	MARKER_TYPE	= "cai.peter.eclipse.j8marker";
	protected static final String	COMMAND_ADD_PREFIX	= "cai.peter.eclipse.j8marker.commandAdd";
	protected static final String	COMMAND_GOTO_PREFIX	= "cai.peter.eclipse.j8marker.commandGoto";
	protected IWorkbenchWindow activeWindow = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		activeWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		return null;
	}


	protected Map<Integer, IMarker> getMarkers()
	{
		Map<Integer, IMarker> result = new HashMap();
		try
		{
			if (getActiveEditor() != null)
			{
				IMarker[] editorMarkers = ResourcesPlugin.getWorkspace().getRoot()
						.findMarkers(MARKER_TYPE,true, IResource.DEPTH_INFINITE);
				for (IMarker marker:editorMarkers)
				{
					int markerNumber = marker.getAttribute("number", -1);
					if (markerNumber < 0)
						marker.delete();
					else
						result.put(new Integer(markerNumber), marker);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	protected int getMarkerNumber(String prefix, String id)
	{
		if (id.startsWith(prefix))
		{
			try
			{
				return Integer.parseInt(id.substring(prefix.length()));
			}
			catch (NumberFormatException nfe)
			{
				nfe.printStackTrace();
			}
		}
		return -1;
	}

	protected IFile getActiveFile()
	{
		IEditorInput input = getActiveInput();
		if (input != null) { return (IFile) input.getAdapter(IFile.class); }
		return null;
	}

	protected IEditorInput getActiveInput()
	{
		ITextEditor editor = getActiveEditor();
		if (editor != null) { return editor.getEditorInput(); }
		return null;
	}

	/**
	 * @return Returns the activeEditor.
	 */
	protected ITextEditor getActiveEditor()
	{
		IEditorPart editor = activeWindow.getActivePage().getActiveEditor();
		if (editor instanceof ITextEditor) 
			return (ITextEditor) editor;
		return null;
	}
}
