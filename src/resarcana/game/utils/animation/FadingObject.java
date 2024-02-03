package resarcana.game.utils.animation;

import org.newdawn.slick.Input;

import resarcana.graphics.DrawablePollable;

public abstract class FadingObject implements DrawablePollable {

	private final float lifetime;
	private float timeLeft;

	public FadingObject(float lifetime) {
		this.lifetime = lifetime;
		this.timeLeft = this.lifetime;
	}

	@Override
	public void poll(Input input, float secounds) {
		this.timeLeft -= secounds;
	}

	public boolean isAlive() {
		return this.timeLeft > 0;
	}

	public float getProgress() {
		return 1 - this.getLifePortionRemaining();
	}

	public float getLifePortionRemaining() {
		return this.timeLeft / this.lifetime;
	}

	public float getTimeRemaining() {
		return this.timeLeft;
	}

	public float getTotalLifetime() {
		return this.lifetime;
	}

}
