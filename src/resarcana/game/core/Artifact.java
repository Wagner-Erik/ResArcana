package resarcana.game.core;

import resarcana.math.Rectangle;
import resarcana.math.Vector;

/**
 * An artifact used in {@link Game}, no special properties in comparison to
 * {@link Tappable}
 * 
 * @author Erik
 *
 */
public class Artifact extends Tappable {

	public static final Rectangle ARTIFACT_HITBOX = new Rectangle(Vector.ZERO, 200, 280);
	public static final Vector ARTIFACT_ESSENCES_POSITION = new Vector(60, -90);

	public Artifact(Game parent, String image) {
		super(parent, image, 1.f);
	}

	@Override
	public Rectangle getRawHitbox() {
		return ARTIFACT_HITBOX;
	}

	@Override
	public Vector getEssencePosition() {
		return ARTIFACT_ESSENCES_POSITION;
	}

}
