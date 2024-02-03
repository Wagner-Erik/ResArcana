package resarcana.game.utils.animation;

import org.newdawn.slick.Graphics;

import resarcana.game.utils.animation.generator.ColorGenerator;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class FadingTrace extends FadingObject {

	private static final float TRACE_REL_OVERLAP = 0.05f;
	private static final float IMAGE_BASE_HEIGHT = 16;
	private static final float TRACE_WIDTH = 12;

	private static final int[] AVAILABLE_ASPECT_RATIOS = new int[] { 1, 2, 4 };

	public static void scheduleImages() {
		Scheduler.getInstance().scheduleResource("animation/trace_straight_1.png");
		Scheduler.getInstance().scheduleResource("animation/trace_straight_2.png");
		Scheduler.getInstance().scheduleResource("animation/trace_straight_4.png");
	}

	private final String image;
	private final Vector position;
	private final float angle, colorOffset;
	private final Rectangle box;
	private final ColorGenerator color;

	public FadingTrace(String image, ColorGenerator color, float colorOffset, Vector p1, Vector p2, float lifetime) {
		super(lifetime);
		this.color = color;
		this.colorOffset = colorOffset;
		this.position = p1.add(p2).div(2);
		this.angle = (float) (p2.sub(p1).clockWiseAng(Vector.RIGHT) * 180 / Math.PI);
		float dist = p1.getDistance(p2);
		this.box = new Rectangle(this.position, dist * (1 + TRACE_REL_OVERLAP), TRACE_WIDTH);
		if (image == null) {
			this.image = "animation/trace_straight_" + getAspectRatio(dist / IMAGE_BASE_HEIGHT) + ".png";
		} else {
			this.image = image;
		}
		// Log.debug("Created trace from " + p1 + " to " + p2 + " with dist " +
		// this.box.width + " at angle " + this.angle);
	}

	public FadingTrace(ColorGenerator color, float colorOffset, Vector p1, Vector p2, float lifetime) {
		this(null, color, colorOffset, p1, p2, lifetime);
	}

	private static int getAspectRatio(float f) {
		int a = AVAILABLE_ASPECT_RATIOS[0], b = a;
		for (int i = 0; i < AVAILABLE_ASPECT_RATIOS.length; i++) {
			b = AVAILABLE_ASPECT_RATIOS[i];
			if (f < b) {
				return (b - f) < (f - a) ? b : a;
			}
			a = b;
		}
		return b;
	}

	@Override
	public void draw(Graphics g) {
		if (this.isAlive()) {
			g.pushTransform();
			g.rotate(this.position.x, this.position.y, -this.angle);
			GraphicUtils.drawImage(g, this.box, ResourceManager.getInstance().getImage(this.image),
					this.color.getColor(this.getLifePortionRemaining(), this.colorOffset));
			g.popTransform();
		}
	}
}
