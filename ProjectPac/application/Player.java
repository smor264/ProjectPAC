package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;



public class Player extends Character{
	int Score = 0;
	private SetArrayList<Main.Direction> heldButtons = new SetArrayList<Main.Direction>();
	private int abilityCharges = 0;
	private Ability ability;
	private int pelletsEaten = 0; // Used exclusively for the snake PlayerCharacter
	private Boost currentBoost = Boost.random;
	private int boostCharges = 2;
	private boolean isAbilityActive = false;
	private boolean isInvisible = false;
	private Circle shield = null;
	private boolean controlsInverted = false;
	private boolean isGhost = false;
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
		superTimeSlow (10, "Super Time Slow!"),

		dash (2, "Dash"),
		superDash (2, "Super Dash!"),

		pelletMagnet (6, "Pellet Magnet"),
		superPelletMagnet (6, "Super Pellet Magnet!"),

		invisibility (6, "Invisibility"),
		superInvisibility (8, "Super Invisibility!"),

		shield (20, "Shield"),
		superShield(40, "Super Shield!"),

		invertControls (6, "Inverted Controls!"),
		randomTeleport (6, "Randomly Teleported!"),
		random (null, "Random?!");

		private Integer duration;
		private String name;

		Boost(Integer duration, String name){
			this.duration = duration; // time in seconds
			this.name = name;
		}

		public Integer duration(){ return duration;}
		public String text(){ return name;}
	}

	public Player(Shape model, double speed, Ability ability) {
		super(model, speed);
		this.ability = ability;
	}

	public Player(Shape model, double speed, boolean isGhost, Color color) {
		super(model,speed);
		this.isGhost = isGhost;
		this.regularColor = color;
		model.setFill(color);
	}

	public boolean getIsGhost() {
		return isGhost;
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

	public void setInvisible(boolean value){
		isInvisible = value;
		model.setOpacity( (value ? 0.35 : 1) );
	}
	public boolean getInvisible(){
		return isInvisible;
	}

	public Circle getShield(){
		return shield;
	}
	public void setShield(Circle shield){
		this.shield = shield;
	}
	public void clearShield(){
		this.shield = null;
	}

	public Boost getBoost(){
		return currentBoost;
	}
	public void setBoost(Boost boost){
		currentBoost = boost;
	}

	public boolean getControlsInverted(){
		return controlsInverted;
	}
	public void setControlsInverted(boolean value){
		controlsInverted = value;
	}

	public boolean isAbilityActive() {
		return isAbilityActive;
	}
	public void setAbilityActive(boolean value) {
		isAbilityActive = value;
	}

}
