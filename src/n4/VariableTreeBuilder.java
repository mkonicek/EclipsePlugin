package n4;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

/** Builds tree model for debugger variables, suitable to be displayed in a . */
public class VariableTreeBuilder {
	
	/** Max depth of the variable tree. */
	public static final int MAX_DEPTH = 3;
	
	/** Build tree model for all variables on the stack. */
	public List<TreeNode> buildForVariablesOnStack(IJavaStackFrame stackFrame) throws DebugException {
		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		for(IVariable variable : stackFrame.getVariables()) {
			TreeNode variableTree = makeTreeNode(variable, (IJavaThread)stackFrame.getThread(), 0);
			nodes.add(variableTree);
		}
		return nodes;
	}
	
	/** Given a debuggee variable, builds a tree model. */
	public TreeNode makeTreeNode(IVariable variable, IJavaThread thread, int depth) throws DebugException {
		return makeTreeNode(variable.getName(), (IJavaValue)variable.getValue(), thread, depth);
	}
	
	/** Given a debuggee variable, builds a tree model. */
	public TreeNode makeTreeNode(String nodeName, IJavaValue variableValue, IJavaThread thread) throws DebugException {
		return makeTreeNode(nodeName, variableValue, thread, 0);
	}
	
	/** Given a debuggee variable, builds a tree model.
	 * @param nodeName the name of the returned TreeNode. 
	 * */
	public TreeNode makeTreeNode(String nodeName, IJavaValue variableValue, IJavaThread thread, int depth) throws DebugException {
		TreeNode result = new TreeNode(nodeName, "");
		
		if (variableValue instanceof IJavaObject) {
			IJavaObject objValue = (IJavaObject)variableValue;
			IJavaValue[] emptyArgs = new IJavaValue[0];
			String defaultSignature = null;
			try {
				IJavaValue valItemToString = objValue.sendMessage("toString", "()Ljava/lang/String;", emptyArgs, thread, defaultSignature);
				result.setText(valItemToString.getValueString());
			} catch (DebugException ex) {
				ex.printStackTrace();
			}
			
			IJavaObject valIt = null;
			try {
				valIt = (IJavaObject)objValue.sendMessage("iterator", "()Ljava/util/Iterator;", emptyArgs, thread, defaultSignature);
			} catch (DebugException ex) {
			}
			if (valIt != null) {
				result.setHasIterator(true);
				TreeNode iteratorNode = result.addChild("iterator", "");
				for (TreeNode iteratorItemNode : iteratorContents(valIt, thread)) {
					iteratorNode.addChild(iteratorItemNode);
				}
			}
			if (depth < MAX_DEPTH && !isPrimitiveType(variableValue.getSignature())) {
				for (IVariable childVar : variableValue.getVariables()) {
					TreeNode childVarNode = makeTreeNode(childVar, thread, depth + 1);
					// only add children which have an iterator
					result.addChild(childVarNode);
				}
			}
		} else {
			// primitive type variable
			result.setText(variableValue.toString());
		}
		return result;
	}

	/** Return true if type signature identifies a primitive type.
	 *  Instances of primitive cannot not be further expanded.*/
	protected boolean isPrimitiveType(String typeSignature) {
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
	
	/** Given a value representing an Iterator in the debuggee, returns all its items. */
	protected Iterable<TreeNode> iteratorContents(IJavaObject valIt, IJavaThread thread) throws DebugException {
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
			result.add(makeTreeNode("[" + i + "]", valItem, thread, 0));
			i++;
		}
		return result;
	}
	
	/** Asynchronously evaluates given expression text. Calls listener when done. */
	protected void evaluate(String exprText, IDebugElement debugContext, IWatchExpressionListener listener, IExpressionManager exprMgr) throws Exception {
		IWatchExpressionDelegate delegate = exprMgr.newWatchExpressionDelegate(debugContext.getModelIdentifier());
		if (delegate == null) {
			throw new Exception("Could not create a watch expression delegate.");
		}
		delegate.evaluateExpression(exprText, debugContext, listener);
	}
}
