package application;

import javafx.scene.shape.Shape;



public class Player extends Character{
	int Score = 0;
	private SetArrayList<Main.Direction> heldButtons = new SetArrayList<Main.Direction>();
	private int abilityCharges = 0;
	private Ability ability;
	private int pelletsEaten = 0; // Used exclusively for the snake PlayerCharacter
	private Boost currentBoost = Boost.dash;
	private int boostCharges = 1;
	private boolean isAbilityActive = false;

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
		timeSlow (6, "Time Slow", 1),
		superTimeSlow (10, "Super Time Slow", 2),
		dash (2, "Dash", 3),
		superDash (2, "Super Dash", 4),
		pelletMagnet (6, "Pellet Magnet",5),
		superPelletMagnet (6, "Super Pellet Magnet",6),
		invertControls (6, "Inverted Controls!",7),
		randomTeleport (6, "Randomly Teleported!",8);

		private Integer duration;
		private String name;
		private Integer number;
		Boost(Integer duration, String name, int number){
			this.duration = duration; // time in seconds
			this.name = name;
			this.number = number;
		}
		public int duration(){ return duration;}
		public String text(){ return name;}
		public int getNumber(){ return number;}
		public Boost getBoostFromNumber(int number) {
			switch(number) {
			case 1: return timeSlow;
			case 2: return superTimeSlow;
			case 3: return superDash;
			case 4: return pelletMagnet;
			case 5: return superPelletMagnet;
			case 6: return invertControls;
			case 7: return randomTeleport;
			default: throw new IllegalArgumentException("Invalid boost number, please enter number between 1 and 7");
			}
		}
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

	public void setBoostCharges(int charges){
		boostCharges = charges;
	}
	public int getBoostCharges(){
		return boostCharges;
	}
	public void incrementBoostCharges(){
		boostCharges++;
	}
	public void decrementBoostCharges(){
		boostCharges--;
	}

	public boolean incrementPelletCounter() {
		pelletsEaten++;
		if (pelletsEaten % 20 == 0) {
			return true;
		}
		else {
			return false;
		}
	}


	public Boost getBoost(){
		return currentBoost;
	}
	public void setBoost(Boost boost){
		this.currentBoost = boost;
	}

	public boolean isAbilityActive() {
		return isAbilityActive;
	}
	public void setAbilityActive(boolean value) {
		isAbilityActive = value;
	}
}
