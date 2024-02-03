package resarcana.game.utils.userinput;

import resarcana.graphics.gui.ImageDisplay;
import resarcana.math.Rectangle;

public interface ImageHolder extends ImageDisplay {

	/**
	 * @return The hitbox of this object, its position is <b>not</b> guranteed to
	 *         have any meaning
	 */
	public Rectangle getHitbox();

}
