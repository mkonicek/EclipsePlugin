package n4;

import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
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

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
//		IWatchExpressionListener listener = new IWatchExpressionListener() {
//			public void watchEvaluationFinished(IWatchExpressionResult resIterator) {
//				System.out.println(resIterator.toString());
//				//result.getErrorMessages()
//				/*MessageDialog.openInformation(
//					window.getShell(),
//					"FirstPlugin",
//					"Value of expr = " + exprValue + ", type: " + exprType);*/
//			}
//		};
//		IExpressionManager exprMgr = DebugPlugin.getDefault().getExpressionManager();
//		try {
//			System.out.println("evaluating first elem");
//			evaluate("((java.lang.Iterable)list).iterator()", getSelectedStackFrame(), listener, exprMgr);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		final ArrayList<TreeNode> rootLevelNodes = new ArrayList<TreeNode>();
		try {
			final IJavaStackFrame stackFrame = getSelectedStackFrame();
			for(IVariable variable : stackFrame.getVariables()) {
				rootLevelNodes.add(makeTreeNode(variable, (IJavaThread)stackFrame.getThread(), 0));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		InspectorWindow inspectorWindow = new InspectorWindow();
		inspectorWindow.open(rootLevelNodes);
		// marshal to UI thread
//		window.getShell().getDisplay().asyncExec(new Runnable() {
//			@Override
//			public void run() {
//				
//			}
//		});
	}
	
	private TreeNode makeTreeNode(IVariable variable, IJavaThread thread, int depth) throws DebugException {
		return makeTreeNode(variable.getName(), (IJavaValue)variable.getValue(), thread, depth);
	}
	
	private TreeNode makeTreeNode(String nodeName, IJavaValue variableValue, IJavaThread thread, int depth) throws DebugException {
		TreeNode result = new TreeNode(nodeName, "");
		
		if (variableValue instanceof IJavaObject) {
			IJavaObject objValue = (IJavaObject)variableValue;
			IJavaValue[] emptyArgs = new IJavaValue[0];
			String defaultSignature = null;
			try {
				IJavaValue valItemToString = objValue.sendMessage("toString", "()Ljava/lang/String;", emptyArgs, thread, defaultSignature);
				result.setValue(valItemToString.getValueString());
			} catch (DebugException ex) {
				ex.printStackTrace();
			}
			
			IJavaObject valIt = null;
			try {
				valIt = (IJavaObject)objValue.sendMessage("iterator", "()Ljava/util/Iterator;", emptyArgs, thread, defaultSignature);
			} catch (DebugException ex) {
			}
			if (valIt != null) {
				TreeNode iteratorNode = result.addChild("iterator", "");
				for (TreeNode iteratorItemNode : iteratorContents(valIt, thread)) {
					iteratorNode.addChild(iteratorItemNode);
				}
			}
			if (depth < 3 && !isPrimitiveType(variableValue.getSignature())) {
				for (IVariable childVar : variableValue.getVariables()) {
					TreeNode childVarNode = makeTreeNode(childVar, thread, depth + 1);
					result.addChild(childVarNode);
				}
			}
		} else {
			// primitive type variable
			result.setValue(variableValue.toString());
		}
		return result;
	}

	/** Gets the VM stack frame selected in the debugger UI. */
	protected IJavaStackFrame getSelectedStackFrame() throws Exception {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context instanceof IJavaStackFrame) {
			return (IJavaStackFrame)context;
		}
		throw new Exception("Can't get current stackframe");
	}

	/** Asynchronously evaluates given expression text. Calls listener when done. */
	private void evaluate(String exprText, IDebugElement debugContext, IWatchExpressionListener listener, IExpressionManager exprMgr) throws Exception {
		IWatchExpressionDelegate delegate = exprMgr.newWatchExpressionDelegate(debugContext.getModelIdentifier());
		if (delegate == null) {
			throw new Exception("Could not create a watch expression delegate.");
		}
		delegate.evaluateExpression(exprText, debugContext, listener);
	}
	
	/** Return true if type signature identifies a primitive type.
	 *  Instances of primitive cannot not be further expanded.*/
	private boolean isPrimitiveType(String typeSignature) {
		if (typeSignature == null) return true;
		if (typeSignature.equals("Ljava/lang/String;")) return true;
		if (typeSignature.equals("Ljava/lang/Integer;")) return true;
		if (typeSignature.equals("Ljava/lang/Double;")) return true;
		if (typeSignature.equals("Ljava/lang/Long;")) return true;
		if (typeSignature.equals("Ljava/lang/Boolean;")) return true;
		if (typeSignature.equals("Ljava/lang/Float;")) return true;
		if (typeSignature.equals("Ljava/lang/Short;")) return true;
		if (typeSignature.equals("Ljava/lang/Byte;")) return true;
		return false;
	}
	
	private Iterable<TreeNode> iteratorContents(IJavaObject valIt, IJavaThread thread) throws DebugException {
		ArrayList<TreeNode> result = new ArrayList<TreeNode>();
		IJavaValue[] emptyArgs = new IJavaValue[0];
		String defaultSignature = null;
		int i = 0;
		// iterate over the iterator
		while(true) {
			IJavaValue valHasNext = valIt.sendMessage("hasNext", "()Z", emptyArgs, thread, defaultSignature);
			if (valHasNext.getValueString() == "false") {
				break;
			}
			// node
			IJavaObject valItem = (IJavaObject)valIt.sendMessage("next", "()Ljava/lang/Object;", emptyArgs, thread, defaultSignature);
			result.add(makeTreeNode(i + "", valItem, thread, 0));
			i++;
		}
		return result;
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