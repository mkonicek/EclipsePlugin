package n4;

import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdi.internal.VirtualMachineManagerImpl;
import org.eclipse.jdt.debug.core.*;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.dialogs.MessageDialog;

import com.sun.jdi.*;

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
	private DebugPlugin debugger;

	/**
	 * The constructor.
	 */
	public SampleAction() {
		debugger = DebugPlugin.getDefault();
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		InspectorWindow inspectorWindow = new InspectorWindow();
		inspectorWindow.open();

		final IJavaStackFrame stackFrame;
		try {
			stackFrame = getSelectedStackFrame();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		IWatchExpressionListener listener= new IWatchExpressionListener() {
			public void watchEvaluationFinished(IWatchExpressionResult result) {
				try {
					//result.getErrorMessages()
					IJavaObject valIt = (IJavaObject)result.getValue();
					IJavaValue[] emptyArgs = new IJavaValue[0];
					String typeSignature = null;
					IJavaThread thread = (IJavaThread)stackFrame.getThread();
					int i = 0;
					while(true) {
						IJavaValue valHasNext = valIt.sendMessage("hasNext", "()Z", emptyArgs, thread, typeSignature);
						if (valHasNext.getValueString() == "false") {
							break;
						}
						// this is a node
						IJavaObject valItem = (IJavaObject)valIt.sendMessage("next", "()Ljava/lang/Object;", emptyArgs, thread, typeSignature);
						String itemTypeSig = valItem.getSignature();
					    IJavaValue valItemToString = valItem.sendMessage("toString", "()Ljava/lang/String;", emptyArgs, thread, typeSignature);
					    // this is node's description
						String valItemString = valItemToString.getValueString();
						System.out.println(i + ". = " + valItemString + ", [" + itemTypeSig + "]");
						i++;
					}
					/*MessageDialog.openInformation(
						window.getShell(),
						"FirstPlugin",
						"Value of expr = " + exprValue + ", type: " + exprType);*/
				} catch (DebugException e) {
					e.printStackTrace();
				}
			}
		};
		IExpressionManager exprMgr = DebugPlugin.getDefault().getExpressionManager();
		try {
			System.out.println("evaluating first elem");
			evaluate("((java.lang.Iterable)list).iterator()", stackFrame, listener, exprMgr);
		} catch (Exception e) {
			e.printStackTrace();
		}
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