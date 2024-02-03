package resarcana.game.utils.animation;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

public abstract class SoundAnimation implements Animation {

	public abstract void playSound();
	
	public SoundAnimation() {
	}

	@Override
	public void draw(Graphics g) {
		// Nothing to do
	}

	@Override
	public void poll(Input input, float secounds) {
		// Nothing to do
	}

	@Override
	public Animation setFixedColor(Color color) {
		return this;
	}

	@Override
	public void start(Color color) {
		this.playSound();
	}

	@Override
	public boolean isRunning() {
		return false;
	}

}
