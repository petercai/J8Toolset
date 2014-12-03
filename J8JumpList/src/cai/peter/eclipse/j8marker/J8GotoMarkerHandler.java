/***********************************************
 * Copyright (c) 2014 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.eclipse.j8marker;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

public class J8GotoMarkerHandler extends BaseHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		super.execute(event);
		
		String id = event.getCommand().getId();
		int number = getMarkerNumber(COMMAND_GOTO_PREFIX, id);
		if( number >= 0 )
			gotoMarker(number);
		return null; 	
	}

	private void gotoMarker(int number)
	{
		Map<Integer, IMarker> markers = getMarkers();
		IMarker marker = markers.get(number);
		if( marker != null )
		{
			try
			{
				IDE.openEditor(activeWindow.getActivePage(), marker, true);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
	}
}
