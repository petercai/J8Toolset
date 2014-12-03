/***********************************************
 * Copyright (c) 2014 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.eclipse.j8marker;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class J8SetMarkerHandler extends BaseHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		super.execute(event);
		
		String id = event.getCommand().getId();
		int number = getMarkerNumber(COMMAND_ADD_PREFIX, id);
		if( number > 0 )
			addMarker(number);
		return null; 	
	}

	private void addMarker(int number)
	{
		ITextEditor editor = getActiveEditor();
		if( editor != null )
		{
			IFile file = getActiveFile();
			/*
			 * marker's attributes
			 */
			Map<String, Object> attributes = new HashMap();
			
			ITextSelection selection = (ITextSelection)editor.getSelectionProvider().getSelection();
			int start = selection.getOffset();
			
			MarkerUtilities.setCharStart(attributes, start);
			MarkerUtilities.setCharEnd(attributes, start);
			MarkerUtilities.setMessage(attributes, "J8Marker "+number);
			attributes.put("file", file);
			attributes.put("number", number);
			
			Map<Integer, IMarker> markers = getMarkers();
			IMarker marker = markers.get(number);
			try
			{
				if( marker != null )
				{
					Integer mkStart = (Integer)marker.getAttribute(IMarker.CHAR_START);
					if( mkStart==null || mkStart != start) marker.delete();
				}
				MarkerUtilities.createMarker(file, attributes, MARKER_TYPE);
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
			
			
		}
		
	}
}
