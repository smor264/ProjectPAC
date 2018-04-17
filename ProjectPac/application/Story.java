package application;

import java.util.HashMap;

public class Story {
	private HashMap<Level, String[]> map = new HashMap<Level, String[]>();
	private String playerName;
	/*Story strings are spliced together with the player's name inbetween any two strings
	 * 
	 * i.e {"Hello ", " How are you?"} would end up being displayed as "Hello playername How are you?"*/
	private final String[] level1 = {""};
	private final String[] medieval1 = {""};
	private final String[] medieval2 = {""};
	private final String[] future1 = {""};
	private final String[] future2 = {""};
	private final String[] ice1 = {""};
	private final String[] ice2 = {""};
	private final String[] rock1 = {""};
	private final String[] rock2 = {""};
	private final String[] garden1 = {""};
	private final String[] garden2 = {""};
	
	public Story(String playerName) {
		map.put(LevelTree.level1, level1);
		map.put(LevelTree.medieval1, medieval1);
		map.put(LevelTree.medieval2, medieval2);
		map.put(LevelTree.future1, future1);
		map.put(LevelTree.future2, future2);
		map.put(LevelTree.ice1, ice1);
		map.put(LevelTree.ice2, ice2);
		map.put(LevelTree.rock1, rock1);
		map.put(LevelTree.rock2, rock2);
		map.put(LevelTree.garden1, garden1);
		map.put(LevelTree.garden2, garden2);
		this.playerName = playerName;
	}

	
	public String getStoryFor(Level level) {
		String[] storyBits = map.get(level);
		String story = "";
		for (int i = 0; i < storyBits.length; i++) {
			if (i != storyBits.length - 1) {
				story += storyBits[i] + playerName; 
			}
			else {
				story += storyBits[i];
			}
		}
		return story;
	}
	
}
