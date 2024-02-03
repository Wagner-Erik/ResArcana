package resarcana.game.utils.animation.generator;

import resarcana.math.Vector;

public interface PathGenerator {

	/**
	 * Get the position at some progress of the path
	 * 
	 * @param progress the progress along the path, from 0.0f to 1.0f
	 */
	public Vector getPosition(float progress);

	/**
	 * Get the angle at some progress of the path
	 * 
	 * @param progress the progress along the path, from 0.0f to 1.0f
	 */
	public int getAngle(float progress);

	public float getScale(float progress);
}
