package resarcana.game.utils.animation.generator;

import resarcana.game.utils.animation.Tracer;
import resarcana.graphics.Pollable;

public abstract class FadingGenerator implements Pollable {

	private Tracer tracer;

	public FadingGenerator() {
	}

	public void setTracer(Tracer tracer) {
		if (this.tracer != null) {
			this.tracer.remove(this);
		}
		this.tracer = tracer;
	}

	public Tracer getTracer() {
		return this.tracer;
	}

	public boolean hasTracer() {
		return this.tracer != null;
	}
}
