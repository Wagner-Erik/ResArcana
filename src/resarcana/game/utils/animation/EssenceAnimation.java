package resarcana.game.utils.animation;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.game.core.Essences;
import resarcana.graphics.Pollable;
import resarcana.graphics.PositionDrawable;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class EssenceAnimation implements PositionDrawable, Pollable {

	/**
	 * Time of the animation in seconds
	 */
	private static final float ANIMATION_TIME = 1.5f;

	private final Rectangle box;
	private final Essences essence;
	private final float scale;

	private float time = ANIMATION_TIME;
	private boolean modeAdd;

	public EssenceAnimation(Rectangle box, Essences essence, float scale) {
		this.box = box;
		this.essence = essence;
		this.scale = scale;
	}

	@Override
	public void drawAt(Graphics g, Vector position) {
		if (this.isRunning()) {
			GraphicUtils.drawImage(g, this.box.scale(this.getCurScale()).modifyCenter(position),
					ResourceManager.getInstance().getImage(this.essence.getImage()), this.getCurColor());
		}
	}

	private Color getCurColor() {
		if (this.modeAdd) { // green
			return new Color(0, 0.8f, 0, 0.5f * (1 - this.time / ANIMATION_TIME) + 0.2f);
		} else { // red
			return new Color(0.8f, 0, 0, 0.5f * (1 - this.time / ANIMATION_TIME) + 0.2f);
		}
	}

	private float getCurScale() {
		return this.scale * (1 + 0.7f * this.time / ANIMATION_TIME);
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.isRunning()) {
			this.time += secounds;
		}
	}

	public void start(boolean modeAdd) {
		this.time = 0;
		this.modeAdd = modeAdd;
	}

	public void stop() {
		this.time = ANIMATION_TIME;
	}

	public boolean isRunning() {
		return this.time < ANIMATION_TIME;
	}
}
