package cai.peter.eclipse.JumpList.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

public class JumpList extends TableViewer {
	

	public JumpList(Composite parent, int style) {
		super(parent, style);
	}

	protected void handleLabelProviderChanged(LabelProviderChangedEvent event)
	{
		Object[] elements = event.getElements();
		if (elements == null)
		{
			refresh();
			return;
		}
		for(Object element : elements)
		{
			if (!(element instanceof IFile)) continue;
			TableItem[] children = getTable().getItems();
			for(TableItem item : children)
			{
				Object data = item.getData();
				if (data == null) continue;
				if (element instanceof IFile
						&& data instanceof IAdaptable
						&& element.equals(((IAdaptable) data)
								.getAdapter(IResource.class)))
				{
					refresh(data);
				}
			}
		}
	}

}
