package resarcana.game.utils.animation.generator;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Input;

import resarcana.game.utils.animation.Particle;
import resarcana.math.Vector;

public class TransferGenerator extends FadingGenerator {

	private final float transferTime, generatorTime, burstDeviation;
	private final int burstQuantity;
	private final Random random = new Random();

	private Vector start = Vector.ZERO, end = Vector.ZERO;
	private ColorGenerator color;
	private int totalAmount, amount, arcSign;
	private String image = "misc/particle.png";

	private float amountLeft, curDeviation;

	public TransferGenerator(float transferTime, float generatorTime, int burstQuantity, float burstDeviation) {
		super();
		this.transferTime = transferTime;
		this.generatorTime = generatorTime;
		this.burstQuantity = burstQuantity;
		this.burstDeviation = burstDeviation;
		Color c = new Color(1, 1, 1, 0.7f);
		this.color = new ColorGenerator() {

			@Override
			public float nextOffset() {
				return 0;
			}

			@Override
			public Color getColor(float parameter, float offset) {
				return c;
			}
		};
	}

//	public void start(ColorGenerator color, int amount, Vector start, Vector end) {
//		this.color = color;
	public void start(String image, int amount, Vector start, Vector end) {
		this.image = image;
		this.totalAmount = amount;
		this.amount = this.totalAmount;
		this.start = start;
		this.end = end;
		this.arcSign = (end.sub(start).clockWiseAng() > Math.PI ? 1 : -1);
		this.curDeviation = this.burstDeviation * (this.start.sub(this.end).abs());
		if (this.hasTracer()) {
			this.getTracer().start();
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.hasTracer()) {
			if (this.amount > 0) {
				this.amountLeft += secounds / this.generatorTime;
				while (this.amountLeft > 0) {
					this.amountLeft--;
					this.amount--;
					int n = (this.burstQuantity > 0 ? this.burstQuantity
							: (this.burstQuantity == 0 ? 1 : -this.burstQuantity * this.totalAmount));
					for (int i = 0; i < n; i++) {
						this.getTracer().add(new Particle(this.image, 0, this.color, this.color.nextOffset(),
								PathFactory.compositePath(PathFactory.getScalingPath(Vector.ZERO, 1, 0.75f),
										PathFactory.getArcPathBetweenPoints(this.start.add(this.getRandomDeviation()),
												this.end, 0, 0.2f * this.arcSign),
										PathFactory.loopPath(PathFactory.getRotatingPath(Vector.ZERO, -10, 10), 3,
												true)),
								this.transferTime));
					}
				}
			} else {
				this.getTracer().stop();
			}
		}
	}

	private Vector getRandomDeviation() {
		return new Vector((this.random.nextFloat() - 0.5f) * this.curDeviation,
				(this.random.nextFloat() - 0.5f) * this.curDeviation);
	}
}
