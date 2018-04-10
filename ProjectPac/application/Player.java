package application;

import javafx.scene.shape.Shape;



public class Player extends Character{
	int Score = 0;
	private SetArrayList<Main.Direction> heldButtons = new SetArrayList<Main.Direction>();
	private int abilityCharges = 0;
	private Ability ability;
	private int pelletsEaten = 0; // Used exclusively for the snake PlayerCharacter
	private Boost currentBoost = Boost.superPelletMagnet;
	private int boostCharges = 1;
	
	/**
	 * PlayerCharacter-specific special actions
	 * */
	public static enum Ability {
		eatGhosts ("Ghost Chomp"),
		wallJump ("Wall Jump"),
		gun ("Anti-Ghost Laser"),
		eatSameColor ("@%t#^g&"),
		snake ("Snake");
		
		private final String text;
		
		private Ability(String text) {
			this.text = text;
		}
		public String text() {return text;}
	}
	
	/**
	 * Special actions usable by any PlayerCharacter
	 * */
	public static enum Boost {
		timeSlow (6, "Time Slow"), 
		superTimeSlow (5, "Super Time Slow"),
		dash (6, "Dash"), 
		superDash (6, "Super Dash"),
		pelletMagnet (6, "Pellet Magnet"), 
		superPelletMagnet (6, "Super Pellet Magnet"),
		invertControls (6, "Inverted Controls!"),
		randomTeleport (6, "Randomly Teleported!");
		
		private Integer duration;
		private String name;
		Boost(Integer duration, String name){
			this.duration = duration; // time in seconds
			this.name = name;
		}
		public int duration(){ return duration;}
		public String text(){ return name;}
	}
	
	public Player(Shape model, double speed, Ability ability) {
		super(model, speed);
		this.ability = ability;
	}

	public void setScore (int score) {
		Score = score;
	}

	public void modifyScore (int change) {
		if (change < 0) {
			if(change > Score){
				Score = 0;
			}
			else {
			Score -= change;
			}
		}
		else {
			Score += change;
		}
	}

	public int getScore(){
		return Score;
	}

	public String getScoreString(){
		return Integer.toString(Score);
	}
	
	public SetArrayList<Main.Direction> getHeldButtons(){
		return heldButtons;
	}
	
	public void setHeldButtons(SetArrayList<Main.Direction> array){
		heldButtons = array;
	}
	
	public Ability getAbility() {
		return ability;
	}
	public void setAbility(Ability ability) {
		this.ability = ability;
	}
	
	public int getAbilityCharges() {
		return abilityCharges;
	}
	
	public void setAbilityCharges(int charges) {
		abilityCharges = charges;
	}
	public void incrementAbilityCharges() {
		abilityCharges++;
	}
	public void decrementAbilityCharges() {
		abilityCharges--;
	}
	
	public boolean incrementPelletCounter() {
		pelletsEaten++;
		if (pelletsEaten % 10 == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	public int getBoostCharges(){
		return boostCharges;
	}
	
	public Boost getBoost(){
		return currentBoost;
	}

}
