package resarcana.game.utils.animation.generator;

import org.newdawn.slick.Color;

public interface ColorGenerator {

	/**
	 * Generates a color based on a parameter (e.g. portion of lifespan left) and an
	 * offset
	 * 
	 * @param parameter the parameter to use for the color generation
	 * @param offset    the offset to use for the color generation
	 * @return the color
	 */
	public Color getColor(float parameter, float offset);

	public float nextOffset();

}
