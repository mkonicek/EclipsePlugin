package n4;

import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public SampleAction() {
	}
	
	/** Gets the VM stack frame selected in the debugger UI. */
	protected IJavaStackFrame getSelectedStackFrame() throws Exception {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context instanceof IJavaStackFrame) {
			return (IJavaStackFrame)context;
		}
		throw new Exception("Can't get current stackframe");
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		VariableTreeBuilder treeBuilder = new VariableTreeBuilder();
		List<TreeNode> rootLevelNodes = null;
		try {
			rootLevelNodes = treeBuilder.buildForVariablesOnStack(getSelectedStackFrame());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (rootLevelNodes != null) {
			InspectorWindow inspectorWindow = new InspectorWindow();
			inspectorWindow.open(rootLevelNodes);
		}
		// marshal to UI thread
//		window.getShell().getDisplay().asyncExec(new Runnable() {
//			@Override
//			public void run() {
//				
//			}
//		});
	}
	
	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}