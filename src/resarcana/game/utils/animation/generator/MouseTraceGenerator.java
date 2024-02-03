package resarcana.game.utils.animation.generator;

import org.newdawn.slick.Input;

import resarcana.game.utils.animation.FadingTrace;
import resarcana.math.Vector;

public class MouseTraceGenerator extends FadingGenerator {

	private static final float MIN_DISTANCE = 10;

	private final float lifetime;
	private final int num;

	private float cur;
	private Vector last = null;

	private ColorGenerator gen;

	public MouseTraceGenerator(ColorGenerator color, float lifetime, int num) {
		this.lifetime = lifetime;
		this.num = num;
		this.gen = color;
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.hasTracer()) {
			this.cur += secounds;
			if (this.cur > this.lifetime / this.num) {
				Vector next = new Vector(input.getMouseX(), input.getMouseY());
				if (this.last == null) {
					this.last = next;
				} else {
					if (next.getDistance(this.last) > MIN_DISTANCE) {
						this.getTracer()
								.add(new FadingTrace(this.gen, this.gen.nextOffset(), this.last, next, this.lifetime));
						this.last = next;
						this.cur = 0;
					}
				}
			}
		}
	}
}
