package resarcana.game.utils.animation;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.game.utils.animation.generator.FadingGenerator;
import resarcana.graphics.DrawablePollable;

public class Tracer implements DrawablePollable {

	private ArrayList<FadingObject> fadings = new ArrayList<FadingObject>();
	private ArrayList<FadingGenerator> generators = new ArrayList<FadingGenerator>();

	private boolean generating = false;

	public Tracer(FadingGenerator generator) {
		generator.setTracer(this);
		this.generators.add(generator);
	}

	public void start() {
		this.generating = true;
	}

	public void stop() {
		this.generating = false;
	}

	public boolean isGenerating() {
		return this.generating;
	}

	@Override
	public void draw(Graphics g) {
		for (FadingObject fading : this.fadings) {
			fading.draw(g);
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		for (FadingObject fading : this.fadings) {
			fading.poll(input, secounds);
		}
		int i = 0;
		while (i < this.fadings.size()) {
			if (this.fadings.get(i).isAlive()) {
				i++;
			} else {
				this.fadings.remove(i);
			}
		}
		if (this.isGenerating()) {
			for (FadingGenerator gen : this.generators) {
				gen.poll(input, secounds);
			}
		}
	}

	public void add(FadingObject trace) {
		this.fadings.add(trace);
	}

	public void add(FadingGenerator fadingGenerator) {
		if (!this.generators.contains(fadingGenerator)) {
			this.generators.add(fadingGenerator);
		} else {
			Log.warn("Add generator " + fadingGenerator + " to " + this + " while it is already present");
		}
	}

	public void remove(FadingGenerator fadingGenerator) {
		if (this.generators.contains(fadingGenerator)) {
			this.generators.remove(fadingGenerator);
		} else {
			Log.warn("Removing generator " + fadingGenerator + " from " + this + " which is not present");
		}
	}

}
