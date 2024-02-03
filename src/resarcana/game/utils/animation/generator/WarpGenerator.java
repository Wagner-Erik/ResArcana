package resarcana.game.utils.animation.generator;

import resarcana.math.Vector;

public interface WarpGenerator {

	/**
	 * Warps a point in the range 0...1, 0...1
	 * 
	 * @param point the point to warp
	 * @param progress the warping progress between 0 and 1
	 * @return the warped point
	 */
	public Vector warpPoint(Vector point, float progress);
}
