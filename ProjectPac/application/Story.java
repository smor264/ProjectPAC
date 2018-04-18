package application;

import java.util.HashMap;

public class Story {
	private HashMap<Level, String[]> map = new HashMap<Level, String[]>();
	private String playerName;
	/*Story strings are spliced together with the player's name inbetween any two strings
	 *
	 * i.e {"Hello ", " How are you?"} would end up being displayed as "Hello playername How are you?"*/
	private final String[] start = {
			  "You've gotta help me! My name's Justin Tyme, and \n"
			+ "the evil Dr. Clocktopus is trying to rewrite all of time! \n"
			+ "He's broken it into pieces and I need your help to reassemble it! \n"
			+ "I’ll give you something to fight back with! Careful though, they only have two uses per area!"};
	private final String[] level1 = {
			  "Wow! You're a natural! \n"
			+ "Your world is safe now, but there are more worlds that need your help!\n"
			+ "Clocktopus has broken both the past and the future! You’ll have to choose which world to help next!"};
	private final String[] medieval1 = {
			  "Looks like you’re back in the past, try not to change the timeline! \n"
			+ "Well, it’s not like we can do more damage than Dr. Clocktopus…"};
	private final String[] medieval2 = {
			  "Excellent! You’ve saved the past and I think you made a good impression with the royalty! \n"
			+ "Another world saved, but still more to go!\n"
			+ "Next you have the choice between a rocky canyon or stroll in a beautiful garden. \n"
			+ "I know which one I would choose!" };
	private final String[] future1 = {
			  "My robot friend has been frozen in time by Dr. Clocktopus, if you can free him I’m sure he will join us!\n"
			+ "He’s been trapped in the city, navigate the maze of skyscrapers and free my friend."};
	private final String[] future2 = {
			  " \"Beep-beep-boop-beep!\" \n"
			+ "I think he’s trying to say thank you! Great work! \n"
			+ "You’ve saved the future, but there are still many more worlds frozen in time. \n"
			+ "Next up you have the choice of an icey world or rocky canyon. \n"
			+ "Looks like you are stuck between a rock and cold place! Haha!"};
	private final String[] ice1 = {
			  "Brrrrrrr, time is not the only thing frozen around here! \n"
			+ "Better wrap up warm for the next one, I have a feeling it won’t be getting any warmer!"};
	private final String[] ice2 = {
			  "Achooo! Nicely done, lets get outta here before I get sick - Achooo! \n"
			+ "Nearly there, just a few more worlds to go!"};
	private final String[] rock1 = {
			  "Such a beautiful formation! You probably didn’t get a chance to stop and appreciate the view, \n"
			+ "must look all the same to you when you’re being chased around!"};
	private final String[] rock2 = {"Is it just me or was that one a little spooky? \n"
			+ "I guess you are always being chased by ghosts so you must be pretty brave! \n"
			+ "Another world saved, only a few left now!"};
	private final String[] garden1 = {
			  "Watch your step! My friend Snac the Snake lives around here somewhere, \n"
			+ "don’t worry he doesn’t bite, unless you step on him…… \n Try to avoid that…"};
	private final String[] garden2 = {
			  "Wow! I thought we would never make it outta there, I was so lost. \n"
			+ "\"Ssssssss\" \nSnac! You’re free, good to see you, will you help us? \n“SsS” (Yes)"};

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
	
	public String getInitialStory(){
		String[] storyBits = start;
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
