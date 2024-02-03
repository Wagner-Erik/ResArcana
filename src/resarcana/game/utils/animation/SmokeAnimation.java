package resarcana.game.utils.animation;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.game.utils.animation.generator.ColorGenerator;
import resarcana.game.utils.animation.generator.FadingGenerator;
import resarcana.game.utils.animation.generator.PathFactory;
import resarcana.game.utils.animation.generator.PathGenerator;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class SmokeAnimation extends FadingGenerator implements Animation, ColorGenerator {

	private final static Rectangle SMOKE_HITBOX = new Rectangle(Vector.ZERO, 128, 128);

	private final Tracer tracer;
	private final float pathTime, smokeTime, deviation;
	private final int smokeCount;
	private final PathGenerator path;
	private final String image;
	private final Rectangle smokeHitbox;

	private final Random random;

	private Color color = Color.white, fixedColor = null;
	private float progress, amountLeft = 0, colorMean = 1;

	public SmokeAnimation(String image, float scale, PathGenerator path, float pathTime, int smokeCount,
			float smokeTime) {
		this.image = image;
		this.smokeHitbox = SMOKE_HITBOX.scale(scale);

		this.path = path;
		this.pathTime = pathTime;
		this.smokeCount = smokeCount;
		this.smokeTime = smokeTime;

		this.deviation = this.path.getPosition(0).sub(this.path.getPosition(1)).abs() * 0.05f * scale;
		this.progress = 2;

		this.tracer = new Tracer(this);
		this.random = new Random();
	}

	@Override
	public void setTracer(Tracer tracer) {
		// Supress change of tracer
		if (!this.hasTracer()) {
			super.setTracer(tracer);
		} else {
			Log.warn("Cannot (should not) change tracer of " + this);
		}
	}

	@Override
	public Animation setFixedColor(Color color) {
		this.fixedColor = color;
		return this;
	}

	@Override
	public void start(Color color) {
		if (this.fixedColor != null) {
			color = this.fixedColor;
		}
		if (color != null) {
			this.color = color;
			this.colorMean = (this.color.r + this.color.g + this.color.b) / 3;
		}
		this.progress = 0;
	}

	@Override
	public boolean isRunning() {
		return this.progress <= 1;
	}

	@Override
	public void draw(Graphics g) {
		this.tracer.draw(g);
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.isRunning()) {
			this.amountLeft += secounds * this.smokeCount / this.pathTime;
			Vector position;
			while (this.amountLeft > 0) {
				this.amountLeft--;
				position = this.path.getPosition(this.progress).add((this.random.nextFloat() - 0.5f) * this.deviation,
						(this.random.nextFloat() - 0.5f) * this.deviation);
				this.getTracer()
						.add(new Particle(this.image, this.random.nextInt(360), this, this.nextOffset(),
								PathFactory.modifySpeedSmooth(PathFactory.getScalingPath(position, 0.5f, 3.5f), 1, 3),
								this.smokeTime * (1.75f * this.random.nextFloat() + 0.25f)).changeHitbox(this.smokeHitbox));
			}
			this.progress += secounds / this.pathTime;
		}
		this.tracer.poll(input, secounds);
	}

	@Override
	public Color getColor(float parameter, float offset) {
		return new Color(this.color.r * parameter + this.colorMean * (1 - parameter),
				this.color.g * parameter + this.colorMean * (1 - parameter),
				this.color.b * parameter + this.colorMean * (1 - parameter), 0.5f * parameter);
	}

	@Override
	public float nextOffset() {
		return 0;
	}

}
