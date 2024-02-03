package resarcana.game.utils.animation.generator;

import resarcana.math.Vector;

/**
 * A {@link WarpGenerator} which warps the (0,0) to (1,1) square into a star
 * shape with star-center {@link #getStarCenter(float)}
 * 
 * @author Erik
 *
 */
public interface StarWarpGenerator extends WarpGenerator {

	public Vector getStarCenter(float progress);
}
