package n4;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.Label;

public class InspectorWindow {

	protected Shell shell;

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
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
		shell.setSize(450, 300);
		shell.setText("SWT Application");
		
		TreeViewer treeViewer = new TreeViewer(shell, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		tree.setBounds(10, 31, 254, 157);
		
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn trclmnName = treeViewerColumn.getColumn();
		trclmnName.setWidth(100);
		trclmnName.setText("Name");
		
		TreeViewerColumn treeViewerColumn_1 = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn trclmnValue = treeViewerColumn_1.getColumn();
		trclmnValue.setWidth(100);
		trclmnValue.setText("Value");
		
		Tree tree_1 = new Tree(shell, SWT.BORDER);
		tree_1.setBounds(272, 31, 85, 85);
		
		Label lblVariables = new Label(shell, SWT.NONE);
		lblVariables.setBounds(10, 10, 55, 15);
		lblVariables.setText("Variables:");
	}
}
