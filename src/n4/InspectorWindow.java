package n4;

import java.util.*;

import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

public class InspectorWindow {

	protected Shell shell;
	protected TreeViewer treeViewer;
	protected Tree tree;
	protected ArrayList<TreeNode> nodes;
	
	// For data binding, jface.TreeViewer is needed. Example for ListViewer:
	// http://www.vogella.de/articles/EclipseDataBinding/ar01s04.html
	
//	private ArrayList<TreeNode> buildTree() {
//		TreeNode rootNode = new TreeNode("rootName", "rootValue");
//		for (int i = 0; i < 5; i++) {
//			rootNode.addChild("subName " + i, "subValue " + i);
//		}
//		ArrayList<TreeNode> result = new ArrayList<TreeNode>();
//		result.add(rootNode);
//		return result;
//	}

	private void fillTree(Tree tree, Iterable<TreeNode> nodes) {
		for (TreeNode node : nodes) {
			TreeItem rootLevelItem = new TreeItem(tree, SWT.NONE);
//			rootLevelItem.addListener(SWT.Expand, new Listener() {
//				@Override
//				public void handleEvent(Event event) {
//					
//				}
//			});
			fillTreeItem(rootLevelItem, node);
		}
	}
	
	private void fillTreeItem(TreeItem treeItem, TreeNode node) {
		treeItem.setText(new String[] { node.getName(), node.getText() });
		for (TreeNode childNode : node.getChildren()) {
			TreeItem childItem = new TreeItem(treeItem, SWT.NONE);
			fillTreeItem(childItem, childNode);
		}
	}

	/**
	 * Open the window.
	 */
	public void open(Iterable<TreeNode> rootLevelNodes) {
		Display display = Display.getDefault();
		createContents();
		fillTree(tree, rootLevelNodes);
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(470, 300);
		shell.setText("Variables");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));

		tree = new Tree(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		tree.setHeaderVisible(true);
	    TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
	    column1.setText("Variable");
	    column1.setWidth(120);
	    TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
	    column2.setText("Value");
	    column2.setWidth(300);
	}
}
