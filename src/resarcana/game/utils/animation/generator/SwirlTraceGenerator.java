package resarcana.game.utils.animation.generator;

import org.newdawn.slick.Input;

import resarcana.game.utils.animation.FadingTrace;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class SwirlTraceGenerator extends FadingGenerator {

	private static final float MAX_LEN = 200;
	private static final float MIN_DISTANCE = MAX_LEN / 10;
	private static final float PORTION_VISIBLE = 0.5f;
	private static final float RESET_PORTION = 0.5f; // should be < 1

	private final ColorGenerator color;
	private final Rectangle box;
	private final float totalLength, velocity, maxLen, lifetime;

	private float curPos = 0, curLen = 0, cur = 0, offset;
	private Vector last;

	public SwirlTraceGenerator(ColorGenerator color, Rectangle box, float offset, float time) {
		this.color = color;
		this.box = box;
		this.totalLength = (this.box.width + this.box.height) * 2;
		this.velocity = this.totalLength / time / (1 - RESET_PORTION);
		this.maxLen = Math.min(MAX_LEN, this.totalLength / 20);
		this.offset = offset;
		this.lifetime = time * PORTION_VISIBLE;
		this.last = this.getPosition(0);
	}

	private Vector getPosition(float pos) {
		float x = Math.min(pos, this.box.width);
		pos -= x;
		if (pos < 0.1f) {
			return new Vector(this.box.x + x, this.box.y + this.offset * (1 - this.curLen / this.maxLen));
		}
		float y = Math.min(pos, this.box.height);
		pos -= y;
		if (pos < 0.1f) {
			return new Vector(this.box.x + this.box.width + this.offset * (1 - this.curLen / this.maxLen),
					this.box.y + y);
		}
		x = Math.min(pos, this.box.width);
		pos -= x;
		x = this.box.width - x;
		if (pos < 0.1f) {
			return new Vector(this.box.x + x,
					this.box.y + this.box.height + this.offset * (1 - this.curLen / this.maxLen));
		}
		y = this.box.height - pos;
		return new Vector(this.box.x + this.offset * (1 - this.curLen / this.maxLen), this.box.y + y);
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.hasTracer()) {
			this.curPos += this.velocity * secounds;
			this.curLen += this.velocity * secounds;
			this.cur += this.velocity * secounds;
			while (this.curPos > this.totalLength) {
				this.curPos -= this.totalLength;
			}
			if (this.curLen > this.maxLen) {
				this.curPos -= this.curLen * RESET_PORTION;
				this.cur = 0;
				this.curLen = 0;
				this.offset *= -1;
				this.last = this.getPosition(this.curPos);
			}
			if (this.cur > MIN_DISTANCE) {
				Vector next = this.getPosition(this.curPos);
				this.getTracer()
						.add(new FadingTrace(this.color, this.curLen / this.maxLen, this.last, next, this.lifetime));
				this.last = next;
				this.cur = 0;
			}
		}
	}
}
