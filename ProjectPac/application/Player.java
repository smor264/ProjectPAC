package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;


/**a Player is any instance of a Character that is controlled through the keyboard.
 * The Player class manages player abilities, boosts, status effects, lives, etc...
 * In multiplayer, some Players act as ghosts*/
public class Player extends Character{
	int Score = 0;
	private SetArrayList<Main.Direction> heldButtons = new SetArrayList<Main.Direction>();
	private int abilityCharges = 0;
	private Ability ability;
	private int pelletsEaten = 0; // Used exclusively for the snake PlayerCharacter
	private Boost currentBoost = Boost.RANDOM;
	private int boostCharges = 2;
	private boolean isAbilityActive = false;
	private boolean isInvisible = false;
	private Circle shield = null;
	private boolean controlsInverted = false;
	private boolean isGhost = false;
	private int lives = 2;
	private int maxLives = 2;
	private int pickupRadius = 0;
	private Shape mouth = new Polygon(-18.0,-18.0, 18.0,-18.0, (18),(0), 0.0,0.0, (18),(0), 18.0,18.0, -18.0,18.0 );

	
	
	/**
	 * PlayerCharacter-specific special actions
	 * */
	public static enum Ability {
		EATGHOSTS ("Ghost Chomp"),
		WALLJUMP ("Wall Jump"),
		LASER ("Anti-Ghost Laser"),
		EATSAMECOLOR ("@%t#^g&"),
		SNAKE ("Snake");

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
		TIMESLOW (6, "Time Slow"),
		SUPERTIMESLOW (10, "Super Time Slow!"),

		DASH (2, "Dash"),
		SUPERDASH (2, "Super Dash!"),

		PELLETMAGNET (6, "Pellet Magnet"),
		SUPERPELLETMAGNET (6, "Super Pellet Magnet!"),

		INVISIBILITY (6, "Invisibility"),
		SUPERINVISIBILITY (8, "Super Invisibility!"),

		SHIELD (20, "Shield"),
		SUPERSHIELD(40, "Super Shield!"),

		INVERTCONTROLS (6, "Inverted Controls!"),
		RANDOMTELEPORT (6, "Randomly Teleported!"),
		RANDOM (null, "Random?!");

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
		this.model.setFill(color);
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
	public void resetBoostCharges(){
		boostCharges = 2;
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
	
	public void resetLives(){
		lives = maxLives;
	}
	public int getLives(){
		return lives;
	}
	public void setMaxLives(int max){
		maxLives = max;
		lives = max;
	}
	public void decrementLives(){
		lives--;
	}
	
	public int getPickupRadius() {
		return pickupRadius;
	}
	public void resetPickupRadius() {
		pickupRadius = 0;
	}
	
	public void setPickupRadius(Boost boost) {
		switch (boost) {
		case PELLETMAGNET:{pickupRadius = 2; break;}
		case SUPERPELLETMAGNET:{pickupRadius = 3; break;}
		default:{ throw new IllegalArgumentException("Only pelletMagnets are valid boosts for this!"); }
		}
	}
	
	public void manageAnimation(int animationFrame, Shape baseModel) {
		if (isGhost) {
			return;
		}
		double xPos;
		double yPos;
		double rotate = model.getRotate();
		if (animationFrame <= 18) {
			xPos = 4*((5.3/18.0) * animationFrame + 12.7);
			yPos = 6*((5.3/18.0) * animationFrame - 5.3);
		}
		else {
			xPos = 4*((-5.3/18.0) * animationFrame + 23.3);
			yPos = 6*((-5.3 / 18.0) * animationFrame + 5.3);
		}
		
		mouth = new Polygon(-18.0,-18.0, 18.0,-18.0, (xPos),(yPos), 0.0,0.0, (xPos),(-yPos), 18.0,18.0, -18.0,18.0 );
		
		Shape newModel = Shape.intersect(baseModel, mouth);
		newModel.setOpacity( (this.getInvisible() ? 0.35 : 1) );
		this.setModel(newModel);
		model.setFill(regularColor);
		model.setRotate(rotate);
		
		if (baseModel instanceof Circle){
			model.setTranslateX(-(xPos/16)+Main.gridSquareSize/8-1);
		}
	}
}
