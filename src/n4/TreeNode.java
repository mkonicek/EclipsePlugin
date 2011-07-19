package n4;

import java.util.ArrayList;

public class TreeNode {
	private String Name;
	private String Value;
	private ArrayList<TreeNode> Children = new ArrayList<TreeNode>();
	
	public TreeNode(String name, String value) {
		super();
		Name = name;
		Value = value;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getValue() {
		return Value;
	}
	public void setValue(String value) {
		Value = value;
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
