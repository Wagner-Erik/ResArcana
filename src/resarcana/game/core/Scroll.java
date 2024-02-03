package resarcana.game.core;

import org.newdawn.slick.Input;

import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class Scroll extends Tappable {

	public static final Rectangle SCROLL_HITBOX = new Rectangle(Vector.ZERO, new Vector(100, 65));
	public static final Vector SCROLL_ESSENCE_POSITION = new Vector(0, 0);

	private boolean markForReturn = false;

	public Scroll(Game parent, String image) {
		super(parent, image, 1.0f);
	}

	@Override
	public void tap() {
		// Return scroll upon usage
		this.markForReturn = true;
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.markForReturn) {
			this.getPlayer().returnScroll(this);
			this.markForReturn = false;
		} else {
			super.poll(input, secounds);
		}
	}

	@Override
	public Rectangle getRawHitbox() {
		return SCROLL_HITBOX;
	}

	@Override
	public Vector getEssencePosition() {
		return SCROLL_ESSENCE_POSITION;
	}

}
