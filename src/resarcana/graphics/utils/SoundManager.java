package resarcana.graphics.utils;

import java.util.Arrays;

import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.util.Log;

import resarcana.graphics.Pollable;

public class SoundManager implements Pollable {

	public static final float RESET_DELAY = -1.0f;
	public static final float THRESHOLD = 0.001f;

	private static SoundManager instance;

	public static SoundManager getInstance() {
		if (instance == null) {
			instance = new SoundManager();
		}
		return instance;
	}

	private static final int SOUND_NUMBER = 26;

	private Music backgroundMusic;

	private Sound menuClickDown, tap, untapAll, typingSound, yourTurn, destroy, attackBow, attackDragon, attackHit,
			attackProtect, draw, discard, gameFinish, moveEssences, buyMonument, buyPlace, cardFromHand, pass,
			menuClickUp, attackDemon, dyingDemonLong, dyingDemonShort, guardDog, lionRoar, dancingSword, swordDropping;

	private float baseVolume = 1.f, volume = 1.f, musicVolume = 1.f;

	private float time;
	private boolean[] playing;
	private boolean musicOn;

	private SoundManager() {
		this.playing = new boolean[SOUND_NUMBER];

		this.menuClickDown = ResourceManager.getInstance().getSound("sounds/menu_click.ogg");
		this.tap = ResourceManager.getInstance().getSound("sounds/tap.ogg");
		this.untapAll = ResourceManager.getInstance().getSound("sounds/untap_all.ogg");
		this.typingSound = ResourceManager.getInstance().getSound("sounds/typing_sound.ogg");
		this.yourTurn = ResourceManager.getInstance().getSound("sounds/your_turn.ogg");

		this.destroy = ResourceManager.getInstance().getSound("sounds/destroy.ogg");
		this.attackBow = ResourceManager.getInstance().getSound("sounds/attack_bow.ogg");
		this.attackDragon = ResourceManager.getInstance().getSound("sounds/attack_dragon.ogg");
		this.attackHit = ResourceManager.getInstance().getSound("sounds/attack_hit.ogg");
		this.attackProtect = ResourceManager.getInstance().getSound("sounds/attack_protect.ogg");

		this.draw = ResourceManager.getInstance().getSound("sounds/draw.ogg"); // TODO: This is the discard sound
		this.discard = ResourceManager.getInstance().getSound("sounds/discard.ogg");
		this.gameFinish = ResourceManager.getInstance().getSound("sounds/game_finish.ogg");
		this.moveEssences = ResourceManager.getInstance().getSound("sounds/move_essences.ogg");
		this.buyMonument = ResourceManager.getInstance().getSound("sounds/buy_monument.ogg");

		this.buyPlace = ResourceManager.getInstance().getSound("sounds/buy_powerplace.ogg");
		this.cardFromHand = ResourceManager.getInstance().getSound("sounds/card_from_hand.ogg"); // TODO: This is the
																									// discard sound
		this.pass = ResourceManager.getInstance().getSound("sounds/pass.ogg");
		this.menuClickUp = ResourceManager.getInstance().getSound("sounds/menu_click.ogg"); // TODO: other sound
		this.attackDemon = ResourceManager.getInstance().getSound("sounds/attack_demon.ogg");

		this.dyingDemonLong = ResourceManager.getInstance().getSound("sounds/demon_dying.ogg");
		this.dyingDemonShort = ResourceManager.getInstance().getSound("sounds/demon_dying_2.ogg");
		this.guardDog = ResourceManager.getInstance().getSound("sounds/guard_dog.ogg");
		this.lionRoar = ResourceManager.getInstance().getSound("sounds/lion_roar.ogg");
		this.dancingSword = ResourceManager.getInstance().getSound("sounds/dancing_sword.ogg");

		this.swordDropping = ResourceManager.getInstance().getSound("sounds/sword_dropping.ogg");

		Music music;
		try {
			music = new Music(ResourceManager.getInstance().normalizeIdentifier("sounds/background_music.ogg"), true);
		} catch (SlickException e) {
			music = null;
		}
		this.backgroundMusic = music;
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.time > RESET_DELAY) {
			this.time = 0;
			this.resetPlaying();
		} else {
			this.time += secounds;
		}
	}

	public void resetPlaying() {
		Arrays.fill(this.playing, !(this.volume > 0));
	}

	public void setBaseVolume(float v) {
		if (v >= 0 && v <= 1) {
			this.baseVolume = v;
		}
	}

	public void setSoundVolume(float v) {
		if (v >= 0 && v <= 1) {
			this.volume = this.baseVolume * v;
			if (this.volume < THRESHOLD) {
				this.volume = 0;
			}
		}
	}

	public void setMusicVolume(float v) {
		if (v >= 0 && v <= 1) {
			if (this.musicVolume == 0 && this.baseVolume * v >= THRESHOLD) {
				this.startMusic();
			}
			this.musicVolume = this.baseVolume * v;
			if (this.musicVolume < THRESHOLD) {
				this.musicVolume = 0;
				if (this.musicOn) {
					this.stopMusic();
				}
			} else {
				if (this.musicOn) {
					this.backgroundMusic.fade(100, this.musicVolume, false);
				}
			}
		}
	}

	public void startMusic() {
		if (!this.musicOn && this.backgroundMusic != null) {
			Log.info("Starting background music");
			this.backgroundMusic.loop(1.f, this.musicVolume);
			this.musicOn = true;
		}
	}

	public void stopMusic() {
		if (this.backgroundMusic != null) {
			Log.info("Stopping background music");
			this.backgroundMusic.stop();
			this.musicOn = false;
		}
	}

	public void playMenuClickDown() {
		// TODO: reenable when sounds are ready
		if (!this.playing[0] && this.playing[0]) {
			this.playing[0] = true;
			this.menuClickDown.play(0.5f, this.volume * 0.25f);
		}
	}

	public void playTap() {
		if (!this.playing[1]) {
			this.playing[1] = true;
			this.tap.play(1, this.volume);
		}
	}

	public void playUntapAll() {
		if (!this.playing[2]) {
			this.playing[2] = true;
			this.untapAll.play(1, this.volume);
		}
	}

	public void playTypingSound() {
		if (!this.playing[3]) {
			this.playing[3] = true;
			this.typingSound.play(1, this.volume * 0.5f);
		}
	}

	public void playMoveEssences() {
		if (!this.playing[4]) {
			this.playing[4] = true;
			this.moveEssences.play(1, this.volume);
		}
	}

	public void playYourTurn() {
		if (!this.playing[5]) {
			this.playing[5] = true;
			this.yourTurn.play(1, this.volume);
		}
	}

	public void playGameFinish() {
		if (!this.playing[6]) {
			this.playing[6] = true;
			this.gameFinish.play(1, this.volume);
		}
	}

	public void playPass() {
		if (!this.playing[7]) {
			this.playing[7] = true;
			this.pass.play(1, this.volume * 0.25f);
		}
	}

	public void playDraw() {
		if (!this.playing[8]) {
			this.playing[8] = true;
			this.draw.play(1, this.volume);
		}
	}

	public void playDiscard() {
		if (!this.playing[9]) {
			this.playing[9] = true;
			this.discard.play(1, this.volume);
		}
	}

	public void playDestroy() {
		if (!this.playing[10]) {
			this.playing[10] = true;
			this.destroy.play(1, this.volume);
		}
	}

	public void playBuyMonument() {
		if (!this.playing[11]) {
			this.playing[11] = true;
			this.buyMonument.play(1, this.volume);
		}
	}

	public void playBuyPowerPlace() {
		if (!this.playing[12]) {
			this.playing[12] = true;
			this.buyPlace.play(1, this.volume);
		}
	}

	public void playCardFromHand() {
		if (!this.playing[13]) {
			this.playing[13] = true;
			this.cardFromHand.play(1, this.volume);
		}
	}

	public void playAttackBow() {
		if (!this.playing[14]) {
			this.playing[14] = true;
			this.attackBow.play(1, this.volume);
		}
	}

	public void playAttackDragon() {
		if (!this.playing[15]) {
			this.playing[15] = true;
			this.attackDragon.play(1, this.volume);
		}
	}

	public void playProtect() {
		if (!this.playing[16]) {
			this.playing[16] = true;
			this.attackProtect.play(1, this.volume);
		}
	}

	public void playAttackHit() {
		if (!this.playing[17]) {
			this.playing[17] = true;
			this.attackHit.play(1, this.volume);
		}
	}

	public void playMenuClickUp() {
		if (!this.playing[18]) {
			this.playing[18] = true;
			this.menuClickUp.play(0.5f, this.volume * 0.25f);
		}
	}

	public void playAttackDemon() {
		if (!this.playing[19]) {
			this.playing[19] = true;
			this.attackDemon.play(1, this.volume);
		}
	}

	public void playDemonDyingLong() {
		if (!this.playing[20]) {
			this.playing[20] = true;
			this.dyingDemonLong.play(1, this.volume);
		}
	}

	public void playDemonDyingShort() {
		if (!this.playing[21]) {
			this.playing[21] = true;
			this.dyingDemonShort.play(1, this.volume * 1.25f);
		}
	}

	public void playGuardDog() {
		if (!this.playing[22]) {
			this.playing[22] = true;
			this.guardDog.play(1, this.volume);
		}
	}

	public void playLionRoar() {
		if (!this.playing[23]) {
			this.playing[23] = true;
			this.lionRoar.play(1, this.volume * 5.0f);
		}
	}

	public void playDancingSword() {
		if (!this.playing[24]) {
			this.playing[24] = true;
			this.dancingSword.play(1, this.volume);
		}
	}

	public void playSwordDropping() {
		if (!this.playing[25]) {
			this.playing[25] = true;
			this.swordDropping.play(1, this.volume);
		}
	}
}
