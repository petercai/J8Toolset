/***********************************************
 * Copyright (c) 2014 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.eclipse.JumpList;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;

public class JumpListElement implements IAdaptable {

	private IEditorReference editorReference;


	public JumpListElement(IEditorReference reference)
	{
		editorReference = reference;
	}


	public IEditorReference getEditorReference()
	{
		return editorReference;
	}


	public Object getAdapter(Class adapter)
	{
		if (IResource.class.equals(adapter))
		{
		
			IEditorPart part = editorReference.getEditor(true);
			if (part != null)
			{
				IEditorInput input = part.getEditorInput();
				if (input instanceof IFileEditorInput) 
				{ 
					IFile file = ((IFileEditorInput) input).getFile();
					return file; 
				}
			}
			return null;
		}
		
		IAdapterManager adapterManager = Platform.getAdapterManager();
		Object adapterReturn = adapterManager.getAdapter(this, adapter);
		return adapterReturn;
	}

}
