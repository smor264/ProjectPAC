package application;

import java.io.File;

import javafx.scene.media.AudioClip;


public class SoundController {
	private String pelletPickupSoundString = "res/PelletPickup.wav";
	private String powerPelletPickupSoundString = "res/PowerPelletPickup.wav";
	private String wallCollideSoundString = "res/WallCollide.wav";
	private String ghostEatenSoundString = "res/GhostEaten.wav";
	private String playerEatenSoundString = "res/PlayerEaten.wav";
	private String gameOverSoundString = "res/GameOver.wav";
	private String levelCompleteSoundString = "res/LevelComplete.wav";
	private String laserSoundString = "res/Laser.wav";
	private String wallJumpSoundString = "res/WallJump.wav";
	private String shieldHitSoundString = "res/ShieldHit.wav";
	private String activateShieldSoundString = "res/ActivateShield.wav";
	
	private AudioClip pelletPickupSound;
	private AudioClip powerPelletPickupSound;
	private AudioClip wallCollideSound;
	private AudioClip ghostEatenSound;
	private AudioClip playerEatenSound;
	private AudioClip gameOverSound;
	private AudioClip levelCompleteSound;
	private AudioClip laserSound;
	private AudioClip wallJumpSound;
	private AudioClip shieldHitSound;
	private AudioClip activateShieldSound;

	public SoundController() {
		//
		pelletPickupSound = new AudioClip(new File(pelletPickupSoundString).toURI().toString());
		powerPelletPickupSound = new AudioClip(new File(powerPelletPickupSoundString).toURI().toString());
		wallCollideSound = new AudioClip(new File(wallCollideSoundString).toURI().toString());
		ghostEatenSound = new AudioClip(new File(ghostEatenSoundString).toURI().toString());
		playerEatenSound = new AudioClip(new File(playerEatenSoundString).toURI().toString());
		gameOverSound = new AudioClip(new File(gameOverSoundString).toURI().toString());
		levelCompleteSound = new AudioClip(new File(levelCompleteSoundString).toURI().toString());
		laserSound = new AudioClip(new File(laserSoundString).toURI().toString());
		wallJumpSound = new AudioClip(new File(wallJumpSoundString).toURI().toString());
		shieldHitSound = new AudioClip(new File(shieldHitSoundString).toURI().toString());
		activateShieldSound = new AudioClip(new File(activateShieldSoundString).toURI().toString());
		
	}
	public void pelletPickup(){
		pelletPickupSound.play();
	}
	
	public void powerPelletPickup(){
		powerPelletPickupSound.play();
	}
	
	public void wallCollide(){
		wallCollideSound.play();
	}
	
	public void ghostEaten(){
		ghostEatenSound.play();
	}
	
	public void playerEaten(){
		playerEatenSound.play();
	}
	
	public void gameOver(){
		gameOverSound.play();
	}
	
	public void levelComplete(){
		levelCompleteSound.play();
	}
	
	public void laser(){
		laserSound.play();
	}
	
	public void wallJump(){
		wallJumpSound.play();
	}
	
	public void shieldHit(){
		shieldHitSound.play();
	}
	
	public void activateShield(){
		activateShieldSound.play();
	}

}
