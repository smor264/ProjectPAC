package application;

import java.util.ArrayList;
import java.util.Arrays;

public class LevelTree {

	public final static Level level1 = new Level("level1");
	public final static Level future1 = new Level("future1");
	public final static Level future2 = new Level("future2");
	public final static Level medieval1 = new Level("medieval1");
	public final static Level medieval2 = new Level("medieval2");
	public final static Level rock1 = new Level("rock1");
	public final static Level rock2 = new Level("rock2");
	public final static Level ice1 = new Level("ice1");
	public final static Level ice2 = new Level("ice2");
	public final static Level garden1 = new Level("garden1");
	public final static Level garden2 = new Level("garden2");

	public final static ArrayList<Level> levelList = new ArrayList<Level>(Arrays.asList(
																				level1, 	//0
																				future1, 	//1
																				future2, 	//2
																				medieval1, 	//3
																				medieval2, 	//4
																				rock1, 		//5
																				rock2, 		//6
																				ice1, 		//7
																				ice2, 		//8
																				garden1, 	//9
																				garden2)); 	//10

	private ArrayList<TreeNode<Level>> treeNodeList = new ArrayList<TreeNode<Level>>();
	private ArrayList<TreeNode<Level>> completedLevels;

	public LevelTree() {
		for (int i = 0; i < levelList.size(); i++) {
			treeNodeList.add(new TreeNode<Level>(levelList.get(i)));
		}

		treeNodeList.get(0).addChild(treeNodeList.get(1)); // Add future1 as a child of level1
			treeNodeList.get(1).addChild(treeNodeList.get(2)); // Add future2 as child of future1
				treeNodeList.get(2).addChild(treeNodeList.get(7)); // Add ice1 as child of future2
					treeNodeList.get(7).addChild(treeNodeList.get(8)); // Add ice2 as child of ice1

				treeNodeList.get(2).addChild(treeNodeList.get(5)); // Add rock1 as child of future2
					treeNodeList.get(5).addChild(treeNodeList.get(6)); // Add rock2 as child of rock1

		treeNodeList.get(0).addChild(treeNodeList.get(3)); // Add medieval1 as child of level1
			treeNodeList.get(3).addChild(treeNodeList.get(4)); // Add medieval2 as child of medieval1
				treeNodeList.get(5).addParent(treeNodeList.get(4)); // Add medieval2 as parent of rock1

				treeNodeList.get(4).addChild(treeNodeList.get(9)); // Add garden1 as child of medieval2
					treeNodeList.get(9).addChild(treeNodeList.get(10)); // Add garden2 as child of garden1

		completedLevels = new ArrayList<TreeNode<Level>>();
	}

	public void addCompletedLevel(Level level){
		completedLevels.add(getTreeNodeEquivalent(level));
	}
	
	public void clearCompletedLevels(){
		completedLevels.clear();
	}
	
	private TreeNode<Level> getTreeNodeEquivalent(Level level){
		for (int i = 0; i < levelList.size(); i++) {
			if (level == levelList.get(i)) {
				return treeNodeList.get(i);
			}
		}
		try {
			throw new Exception("Invalid level name");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isUnlocked(Level level) {
		if (getTreeNodeEquivalent(level).getParents().isEmpty()) {
			System.out.println("level is unlocked because parents are null");
			return true;
		}

		for (TreeNode<Level> parent : getTreeNodeEquivalent(level).getParents()) {
			if (completedLevels.contains(parent)) {

				System.out.println("level is unlocked because a parent level was beaten");
				return true;
			}
		}
		return false;
	}

	public boolean isCompleted(Level level) {
		return completedLevels.contains(getTreeNodeEquivalent(level));
	}

}
