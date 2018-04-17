package application;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
	
/**
 * The TreeNode class represents a node on a tree structure. 
 * Each node contains some data, a list of parents, and a list of children.
 * **/
public class TreeNode<T> {
	T data;
	List<TreeNode<T>> parents;
	List<TreeNode<T>> children;

	public TreeNode(T data) {
		this.data = data;
		this.children = new ArrayList<TreeNode<T>>();
		this.parents = new ArrayList<TreeNode<T>>();
	}

	public TreeNode<T> addChild(T child) {
		TreeNode<T> childNode = new TreeNode<T>(child);
		childNode.parents.add(this);
		this.children.add(childNode);
		return childNode;
	}
	public void addChild(TreeNode<T> child) {
		child.parents.add(this);
		this.children.add(child);
	}
	public void addParent(TreeNode<T> parent) {
		this.parents.add(parent);
		parent.parents.add(this);
	}

	public ArrayList<TreeNode<T>> addChildren(List<T> children) {
		ArrayList<TreeNode<T>> childrenList = new ArrayList<TreeNode<T>>();
		for (T child : children) {
			TreeNode<T> childNode = new TreeNode<T>(child);
			childNode.parents.add(this);
			this.children.add(childNode);
			childrenList.add(childNode);
		}
		return childrenList;
	}

	public boolean isParentOf(TreeNode<T> node) {
		if (this.children.contains(node)) {
			return true;
		}
		else { return false; }
	}

	public boolean isChildOf(TreeNode<T> node) {
		if (this.parents.contains(node)) {
			return true;
		}
		else { return false; }
	}

	public ArrayList<TreeNode<T>> getChildren(){
		ArrayList<TreeNode<T>> childrenList = new ArrayList<TreeNode<T>>();

		for (TreeNode<T> node : this.children) {
			childrenList.add(node);
		}

		return childrenList;
	}

	public List<TreeNode<T>> getParents() {
		return this.parents;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}
	
	public T getData() {
		return data;
	}

}
