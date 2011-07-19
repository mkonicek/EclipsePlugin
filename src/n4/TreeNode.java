package n4;

import java.util.ArrayList;

public class TreeNode {
	private String name;
	private String text;
	private boolean hasIterator = false;
	private ArrayList<TreeNode> Children = new ArrayList<TreeNode>();
	
	public TreeNode(String name, String value) {
		super();
		this.name = name;
		this.text = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getText() {
		return text;
	}
	public void setText(String value) {
		text = value;
	}
	public boolean hasIterator() {
		return hasIterator;
	}
	public void setHasIterator(boolean hasIterator) {
		this.hasIterator = hasIterator;
	}
	
	public Iterable<TreeNode> getChildren() {
		return Children;
	}
	public TreeNode addChild(TreeNode child) {
		Children.add(child);
		return child;
	}
	public TreeNode addChild(String name, String value) {
		TreeNode newChild = new TreeNode(name, value);
		Children.add(newChild);
		return newChild;
	}
}
