package resarcana.game.utils.animation.generator;

import java.util.Random;

import org.newdawn.slick.Input;

import resarcana.game.utils.animation.Particle;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class RandomParticleGenerator extends FadingGenerator {

	private static final float DEVIATION_PART = 0.5f;

	private final Random random;
	private final Rectangle box;
	private final float lifetime, density, velocity;
	private final Vector center;
	private final ColorGenerator color;

	private float amountLeft;

	public RandomParticleGenerator(Rectangle box, ColorGenerator color, float lifetime, float density, float velocity) {
		super();
		this.random = new Random();
		this.box = box;
		this.center = this.box.getCenter();
		this.color = color;
		this.lifetime = lifetime;
		this.density = density;
		this.velocity = velocity;
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.hasTracer()) {
			this.amountLeft += secounds * this.density;
			Vector position;
			while (this.amountLeft > 0) {
				this.amountLeft--;
				position = this.box.getPositionAtOutline(this.random.nextFloat());
				this.getTracer()
						.add(new Particle("misc/particle_star.png", this.random.nextInt(72), this.color,
								this.color.nextOffset(),
								PathFactory.getBalisitcPath(position, position.sub(this.center)
										.rotate((float) ((this.random.nextFloat() - 0.5f) * Math.PI * DEVIATION_PART))
										.getDirection()
										.mul(this.velocity
												* (this.random.nextFloat() * 2 * DEVIATION_PART + 1 - DEVIATION_PART)),
										(this.random.nextFloat() * 2 * DEVIATION_PART + 1 - DEVIATION_PART) * this.lifetime),
								(this.random.nextFloat() * 2 * DEVIATION_PART + 1 - DEVIATION_PART) * this.lifetime));
			}
		}
	}
}
