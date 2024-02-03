package resarcana.game.core;

import org.newdawn.slick.Color;

import resarcana.game.utils.userinput.ImageHolder;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public enum Essences implements ImageHolder {

	ELAN(Color.red), LIFE(Color.green), CALM(Color.blue), DEATH(Color.black), GOLD(Color.yellow);

	public static final Rectangle ESSENCES_HITBOX = new Rectangle(Vector.ZERO, 128, 128);

	private final String image;
	public final Color color;

	private Essences(Color c) {
		this.image = "essence/essence_" + this.toString().toLowerCase() + ".png";
		this.color = c;
		Scheduler.getInstance().scheduleResource(this.image);
	}

	@Override
	public String getImage() {
		return this.image;
	}

	@Override
	public Rectangle getHitbox() {
		return ESSENCES_HITBOX;
	}

}
