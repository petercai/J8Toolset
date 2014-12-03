/***********************************************
 * Copyright (c) 2014 Peter Cai                *
 * All rights reserved.                        *
 ***********************************************/
package cai.peter.eclipse.JumpList.views;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import cai.peter.eclipse.JumpList.JumpListElement;


public class JumpListView extends ViewPart
{
	private static final String	ICONS_SORT				= "icons/full/clcl16/alpha_mode.gif";
	private static final String	ICONS_OPEN_WORKINGSET	= "icons/full/clcl16/open_workingset.gif";
	private static final String	ICONS_CREATE_WORKINGSET	= "icons/full/clcl16/create_workingset.gif";
	private static final String	ICONS_CLOSEALL			= "icons/full/clcl16/closeall.gif";
	private static final String	ICONS_CLOSE				= "icons/full/clcl16/close.gif";	
	private final class OpenWorkingSetAction extends
			org.eclipse.jface.action.Action
	{

		{
			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(JumpListView.JUMPLIST_BUNDLE_ID, ICONS_OPEN_WORKINGSET);
			if (imageDescriptor != null) setImageDescriptor(imageDescriptor);
			setDescription("Open workingset"); //$NON-NLS-1$
			setToolTipText("Open workingset"); //$NON-NLS-1$
			setText("Open workingset"); //$NON-NLS-1$
		}

		public void run()
		{
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
			
			IWorkingSetManager workingSetManager = workbench.getWorkingSetManager();
			IWorkbenchPart activePart = page.getActivePart();
			IWorkbenchPartSite site = activePart.getSite();
			Shell shell = site.getShell();
			IWorkingSetSelectionDialog dialog = workingSetManager.createWorkingSetSelectionDialog(
							shell,
							true);
			dialog.open();
			IWorkingSet sets[] = dialog.getSelection();
			if (sets == null) { return; }
			String sOpenAction = "Exceptions occurred attempting to open editor(s).";
			MultiStatus status = new MultiStatus(
					JumpListView.JUMPLIST_BUNDLE_ID,
					IStatus.ERROR,
					sOpenAction, null); //$NON-NLS-1$
			for (IWorkingSet workingSet : sets)
			{
				IAdaptable[] elements = workingSet.getElements();
				for (IAdaptable element : elements)
				{
					if (element instanceof ICompilationUnit)
					{
						IResource res = ((ICompilationUnit)element).getResource();
						element = res;
					}
					
					if (element instanceof IFile)
					{
						try
						{
							IDE.openEditor(page, (IFile) element);
						}
						catch (PartInitException e)
						{
							status.merge(e.getStatus());
						}
					}
				}
			}
			if (!status.isOK())
			{
				Shell shell2 = activeWorkbenchWindow.getShell();
				String sErrT = "Error Opening Working Set";
				String message = status.getMessage();
				ErrorDialog.openError(
								shell2,
								sErrT, message, status); //$NON-NLS-1$
			}
		}
	}

	private final class CreateWorkingSetAction extends
			org.eclipse.jface.action.Action
	{

		{
			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(JumpListView.JUMPLIST_BUNDLE_ID, ICONS_CREATE_WORKINGSET);
			if( imageDescriptor!=null )
				setImageDescriptor(imageDescriptor);
			setDescription("Create new working set"); //$NON-NLS-1$
			setToolTipText("Create new working set"); //$NON-NLS-1$
			setText("Create new working set"); //$NON-NLS-1$
		}

		public void run()
		{
			JumpListView.this.createWorkingSetFromSelectedEditors();
		}
	}

	private final class CloseAllAction extends org.eclipse.jface.action.Action
	{

		{
			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(JumpListView.JUMPLIST_BUNDLE_ID, ICONS_CLOSEALL);
			if (imageDescriptor != null) setImageDescriptor(imageDescriptor);
			setDescription("Close all"); //$NON-NLS-1$
			setText("Close all"); //$NON-NLS-1$
		}

		public void run()
		{
			JumpListView.this.closeAllEditors();;
		}
	}

	private final class CloseAction extends org.eclipse.jface.action.Action
	{

		{
			ImageDescriptor imageDescriptorFromPlugin = AbstractUIPlugin.imageDescriptorFromPlugin(JumpListView.JUMPLIST_BUNDLE_ID, ICONS_CLOSE);
			if (imageDescriptorFromPlugin != null) setImageDescriptor(imageDescriptorFromPlugin);
			setDescription("Close the selected editor"); //$NON-NLS-1$
			setText("Close"); //$NON-NLS-1$
		}

		@Override
		public void run()
		{
			JumpListView.this.closeSelectedEditors();
		}
	}

	private final class PropertyListener implements IPropertyListener
	{
		@Override
		public void propertyChanged(Object source, int propId)
		{
			if (source instanceof IEditorPart
					&& (propId == IWorkbenchPart.PROP_TITLE || propId == IEditorPart.PROP_DIRTY))
			{
				refreshLabel((IEditorPart) source);
			}
		}
	}

	private final class JumpListViewLabelProvider extends LabelProvider
	{
		public Image getImage(Object element)
		{
			if (element instanceof JumpListElement) 
			{ 
				IEditorReference editorReference = ((JumpListElement) element)
					.getEditorReference();
				return editorReference.getTitleImage(); 
			}
			return super.getImage(element);
		}

		public String getText(Object element)
		{
			if (element instanceof JumpListElement)
			{
				IEditorReference reference = ((JumpListElement) element)
						.getEditorReference();
				StringBuffer buffer = new StringBuffer();
				if (reference.isDirty())
				{
					buffer.append("*");
				}
				buffer.append(reference.getTitle());
				return buffer.toString();
			}
			return super.getText(element);
		}
	}

	private final class PageListener implements IPageListener
	{
		@Override
		public void pageOpened(IWorkbenchPage page)
		{
		}

		public void pageClosed(IWorkbenchPage page)
		{
		}

		@Override
		public void pageActivated(IWorkbenchPage page)
		{
			refreshContents();
		}
	}

	private final class PartListener implements IPartListener
	{
		@Override
		public void partOpened(IWorkbenchPart part)
		{
			if( part instanceof IEditorPart )
				addEditorToViewer((IEditorPart)part);
			else if( part == JumpListView.this )
				refreshContents();
		}

		@Override
		public void partDeactivated(IWorkbenchPart part)
		{
		}

		@Override
		public void partClosed(IWorkbenchPart part)
		{
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part)
		{
			if( part instanceof IEditorPart)
				editorSelected((IEditorPart)part);
		}

		@Override
		public void partActivated(IWorkbenchPart part)
		{
			if( part instanceof IEditorPart)
				editorSelected((IEditorPart)part);
		}
	}

	private final class MenuListener implements IMenuListener
	{
		public void menuAboutToShow(IMenuManager mgr)
		{
			fillContextMenu(mgr);
		}
	}


	private JumpList			editorList;
//	private EditorViewContentProvider	editorContentProvider;
	/**
	 * The listeners for the editor list.
	 */
	private PartListener	partListener;
	private PageListener	pageListener;


	public static JumpListView[] getEditorLists()
	{
		IWorkbench workbench = PlatformUI.getWorkbench();
		List<IViewPart> views = new ArrayList<>();
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows())
		{
			IWorkbenchPage activePage = window.getActivePage();
			IViewPart view = activePage.findView(JumpListView.JUMPLIST_VIEW_ID);
			if (view != null) views.add(view);
		}
		return (JumpListView[]) views.toArray(new JumpListView[views.size()]);
	}


	public static IFile getFile(IEditorPart editor)
	{
		IFile file = null;
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) 
		{ 
			file = ((IFileEditorInput)input).getFile();
		}
		return file;
	}


	private static IEditorReference getReference(IEditorPart part)
	{
		PartSite site = (PartSite) part.getSite();
		return (IEditorReference) site.getPartReference();
	}


	public JumpListView()
	{
		super();
		partListener = new PartListener();
		pageListener = new PageListener();
	}

	public void createPartControl(Composite parent)
	{
		// add lister to workbench page part
		IWorkbenchPage page = getPage();
		page.addPartListener(partListener);
		
		createViewer(parent);
		createContextMenu(editorList.getControl());
		createToolbarActions();
		
		
		// add lister to workbench window
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		activeWorkbenchWindow.addPageListener(pageListener);
	}
	private Map<IEditorReference, JumpListElement>	editorReferenceMap	= new HashMap<IEditorReference, JumpListElement>();
	private void createViewer(Composite parent)
	{
		editorList = new JumpList(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		
		// decorate & lable provider
//		EditorViewLabelProvider editorViewLabelProvider = new EditorViewLabelProvider();
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		IDecoratorManager decoratorManager = workbench.getDecoratorManager();
		DecoratingLabelProvider labelProvider = new DecoratingLabelProvider(
				new JumpListViewLabelProvider(),
				decoratorManager.getLabelDecorator());
		
		editorList.setLabelProvider(labelProvider);
		
		//label provider listener
		labelProvider.addListener(new ILabelProviderListener()
		{
			@Override
			public void labelProviderChanged(LabelProviderChangedEvent event)
			{
				Object[] elements = event.getElements();
				if (elements == null)
				{
					refreshContents();
					return;
				}
				for (int i = 0; i < elements.length; i++)
				{
					Object element = elements[i];
					if (element instanceof JumpListElement)
					{
						IEditorPart editor = ((JumpListElement) element)
								.getEditorReference().getEditor(true);
						if (editor != null)
						{
							refreshLabel(editor);
						}
					}
				}
			}
		});
		
		// talbe: content
		Table table = editorList.getTable();
		// mouse listener
		table.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseUp(MouseEvent e)
			{
			}
			
			@Override
			public void mouseDown(MouseEvent e)
			{
				bringToTopSelectedEditor();
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				activateSelectedEditor();
			}
		});
		
//		editorContentProvider = new EditorViewContentProvider();
		editorList.setContentProvider(new IStructuredContentProvider(){

			@Override
			public void dispose()
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput)
			{
				
			}

			@Override
			public Object[] getElements(Object inputElement)
			{
				IEditorReference references[] = ((IWorkbenchPage) inputElement)
						.getEditorReferences();
				JumpListElement elements[] = new JumpListElement[references.length];
				JumpListElement element;
				editorReferenceMap.clear();
				for (int i = 0, numReferences = references.length; i < numReferences; i++)
				{
					element = new JumpListElement(references[i]);
					editorReferenceMap.put(references[i], element);
					elements[i] = element;
				}
				return elements;
			}});
		
		IWorkbenchPage page = getPage();
		editorList.setInput(page);
		
		initViewerContents();
		
		IWorkbenchPartSite site = getSite();
		site.setSelectionProvider(editorList);
		Control control = editorList.getControl();
		control.addKeyListener(new KeyListener()
		{
			@Override
			public void keyReleased(KeyEvent event)
			{
				if (event.stateMask == 0)
				{
					if (event.character == SWT.DEL)
					{
						closeSelectedEditors();
					}
					else if (event.keyCode == SWT.F5)
					{
						refreshContents();
					}
					else if (event.character == SWT.CR)
					{
						bringToTopSelectedEditor();
					}
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e)
			{
			}
		});
	}


	private void initViewerContents()
	{
		IEditorReference[] references = getPage().getEditorReferences();
		for (int i = 0; i < references.length; i++)
		{
			addReferenceToViewer(references[i]);
		}
	}

	ViewerSorter viewerSorter=new ViewerSorter();;
	private void createToolbarActions()
	{
		IViewSite viewSite = getViewSite();
		IActionBars actionBars = viewSite.getActionBars();
		IToolBarManager toolbar = actionBars.getToolBarManager();
		
		toolbar.add(new CloseAction());
		toolbar.add(new CloseAllAction());
		
		toolbar.add(new org.eclipse.jface.action.Action(){

			{
				ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(JumpListView.JUMPLIST_BUNDLE_ID, ICONS_SORT);
				if( imageDescriptor!=null)
					setImageDescriptor(imageDescriptor);
				
				setChecked(false);
				setSort(false);
			}
			protected void setSort(boolean sort) {
				editorList.setSorter(sort ? viewerSorter : null);
				
				setToolTipText(sort ? "Do not sort" : "Sort"); //$NON-NLS-1$ //$NON-NLS-2$
				setDescription(sort ? "Disable sorting" : "Enable sorting"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			public void run()
			{
				setSort(isChecked());
			}});
		
		toolbar.add(new CreateWorkingSetAction());
		toolbar.add(new OpenWorkingSetAction());
	}


	private void createContextMenu(Control menuControl)
	{
		MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new MenuListener());
		Menu menu = menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);
		// register the context menu such that other plugins may contribute to
		// it
		getSite().registerContextMenu(menuMgr, editorList);
	}


	protected void fillContextMenu(IMenuManager menu)
	{
		menu.add(new CloseAction());
		menu.add(new CloseAllAction());
		menu.add(new Separator());
		menu.add(new CreateWorkingSetAction());
		menu.add(new OpenWorkingSetAction());
		menu.add(new Separator());
		menu.add(new org.eclipse.jface.action.Action(){

			{
				setDescription("Refresh"); //$NON-NLS-1$
				setText("Refresh"); //$NON-NLS-1$
			}
			public void run()
			{
				JumpListView.this.refreshContents();
			}});
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public void closeAllEditors()
	{
		IWorkbenchPage page = getPage();
		page.closeAllEditors(true);
		refreshContents(); // Workaround to lack of IEditorReference.close()
							// notifications
	}

	public void closeSelectedEditors()
	{
		StructuredSelection selection = (StructuredSelection)editorList.getSelection();
		Iterator iter = selection.iterator();
		while (iter.hasNext())
		{
			JumpListElement element = (JumpListElement) iter.next();
			IEditorPart editor = getRestoredEditor(element.getEditorReference());
			if (editor != null) 
				getPage().closeEditor(editor, true);
		}
		refreshContents(); // Workaround to lack of IEditorReference.close()
							// notifications
	}


	public void createWorkingSetFromSelectedEditors()
	{
		// get editorView selection
		StructuredSelection selection = (StructuredSelection) editorList.getSelection();
		Iterator<JumpListElement> itEditorList = selection.iterator();
		List<IFile> elementList = new ArrayList<IFile>();
		JumpListElement element;
		IEditorPart editor;
		while (itEditorList.hasNext())
		{
			element = itEditorList.next();
			IEditorReference editorReference = element.getEditorReference();
			editor = getRestoredEditor(editorReference);
			if (editor != null)
			{
				IFile file = getFile(editor);
				if (file != null)
				{
					elementList.add(file);
				}
			}
		}
		if (elementList.size() == 0) 
			return;
		
		IAdaptable adaptables[] = new IAdaptable[elementList.size()];
		elementList.toArray(adaptables);
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkingSetManager manager = workbench.getWorkingSetManager();
		Shell shell = getViewSite().getShell();
		IWorkingSetSelectionDialog dialog = manager
				.createWorkingSetSelectionDialog(shell, false);
		if (dialog.open() == Window.OK)
		{
			IWorkingSet[] workingSets = dialog.getSelection();
			for( IWorkingSet workingSet : workingSets )
			{
				workingSet.setElements(adaptables);
				manager.addWorkingSet(workingSet);
			}
		}
	}


	protected IEditorPart getRestoredEditor(IEditorReference reference)
	{
		IEditorPart part = reference.getEditor(true);
		if (part == null)
		{
			removeEditorReference(reference);
		}
		return part;
	}


	protected IWorkbenchPage getPage()
	{
		return getSite().getPage();
	}


	protected IEditorReference getSelectedEditor()
	{
		ISelectionProvider provider = getViewSite().getSelectionProvider();
		if (provider == null) { return null; }
		IStructuredSelection selection = (IStructuredSelection) provider
				.getSelection();
		if (selection.size() != 1) { return null; }
		return ((JumpListElement) selection.getFirstElement())
				.getEditorReference();
	}


	public void editorClosed(IEditorPart editor)
	{
		removeEditorReference(getReference(editor));
	}


	public void removeEditorReference(IEditorReference reference)
	{
		reference.removePropertyListener(propertyListener);
		editorList.remove(editorReferenceMap.get(reference));
		editorReferenceMap.remove(reference);
	}


	public void addEditorToViewer(IEditorPart editor)
	{
		TableItem[] items = editorList.getTable().getItems();
		JumpListElement element;
		IEditorReference reference = getReference(editor);
		// Ensure that the reference for this editor part hasn't already
		// been added.
		for (int i = 0, numItems = items.length; i < numItems; i++)
		{
			element = (JumpListElement) items[i].getData();
			if (element.getEditorReference() == reference) { return; }
		}
		addReferenceToViewer(reference);
	}


	PropertyListener propertyListener = new PropertyListener();
	public static final String	JUMPLIST_BUNDLE_ID	= "J8JumpList";
	public static final String		JUMPLIST_VIEW_ID	= "cai.peter.eclipse.view.J8JumpList";	//$NON-NLS-1$
	public void addReferenceToViewer(IEditorReference reference)
	{
		reference.addPropertyListener(propertyListener);
		JumpListElement element = new JumpListElement(reference);
		editorReferenceMap.put(reference, element);
//		element = editorContentProvider.addElement(reference);
		editorList.add(element);
	}

	public void refreshContents()
	{
		editorList.getTable().removeAll();
		editorList.refresh();
	}


	public void refreshLabel(IEditorPart part)
	{
		editorList.refresh(editorReferenceMap.get(getReference(part)));
	}


	public void activateSelectedEditor()
	{
		IEditorReference reference = getSelectedEditor();
		if (reference == null) { return; }
		IEditorPart editor = getRestoredEditor(reference);
		getPage().activate(editor);
		updateStatusLineFor(editor);
	}


	public void bringToTopSelectedEditor()
	{
		IEditorReference reference = getSelectedEditor();
		if (reference == null) { return; }
		IEditorPart editor = getRestoredEditor(reference);
		if (editor != getPage().getActiveEditor())
		{
			getPage().bringToTop(editor);
			updateStatusLineFor(editor);
		}
	}


	public void updateStatusLineFor(IEditorPart editor)
	{
		IFile file = getFile(editor);
		String message = null;
		if (file != null)
		{
			message = file.getFullPath().toString();
		}
		getViewSite().getActionBars().getStatusLineManager()
				.setMessage(message);
	}


	public void editorSelected(IEditorPart editor)
	{
		IEditorReference selectedEditor = getSelectedEditor();
		IEditorReference newEditor = getReference(editor);
		if (selectedEditor != null)
		{
			if (selectedEditor.equals(newEditor)) { return; }
		}
		JumpListElement element = editorReferenceMap.get(newEditor);
		if (element != null)
		{
			editorList.setSelection(new StructuredSelection(element), true);
		}
	}

	public void setFocus()
	{
		editorList.getControl().setFocus();
	}

	public void dispose()
	{
		super.dispose();
		getPage().removePartListener(partListener);
		IEditorReference[] references = getPage().getEditorReferences();
		for (IEditorReference reference : references)
			reference.removePropertyListener(propertyListener);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.removePageListener(pageListener);
	}
}
