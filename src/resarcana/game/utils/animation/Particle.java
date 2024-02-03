package resarcana.game.utils.animation;

import org.newdawn.slick.Graphics;

import resarcana.game.utils.animation.generator.ColorGenerator;
import resarcana.game.utils.animation.generator.PathGenerator;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class Particle extends FadingObject {

	private static final Rectangle PARTICLE_BOX = new Rectangle(Vector.ZERO, 64, 64);

	private final String image;
	private final ColorGenerator color;
	private final PathGenerator path;
	private final float colorOffset, rotation;

	private Rectangle hitbox = PARTICLE_BOX;

	public Particle(String image, float rotation, ColorGenerator color, float colorOffset, PathGenerator path,
			float lifetime) {
		super(lifetime);
		this.image = image;
		this.rotation = rotation;
		this.color = color;
		this.colorOffset = colorOffset;
		this.path = path;
	}

	public Particle changeHitbox(Rectangle hitbox) {
		if (hitbox != null) {
			this.hitbox = hitbox.modifyCenter(Vector.ZERO);
		}
		return this;
	}

	@Override
	public void draw(Graphics g) {
		if (this.isAlive()) {
			g.pushTransform();
			GraphicUtils.translate(g, this.path.getPosition(this.getProgress()));
			float rot = this.path.getAngle(this.getProgress()) + this.rotation;
			if (rot != 0) {
				g.rotate(0, 0, rot);
			}
			GraphicUtils.drawImage(g, this.hitbox.scale(this.path.getScale(this.getProgress())),
					ResourceManager.getInstance().getImage(this.image),
					this.color.getColor(this.getLifePortionRemaining(), this.colorOffset));
			g.popTransform();
		}
	}
}
