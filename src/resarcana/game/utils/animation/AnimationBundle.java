package resarcana.game.utils.animation;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import javafx.util.Pair;

public class AnimationBundle implements Animation {

	private ArrayList<Pair<Animation, Float>> content, toStart;

	private float time = Float.NaN;

	private Color fixedColor = null, color = Color.white;

	public AnimationBundle() {
		this.content = new ArrayList<Pair<Animation, Float>>();
		this.toStart = new ArrayList<Pair<Animation, Float>>();
	}

	public AnimationBundle(Animation a1) {
		this();
		this.add(a1);
	}

	public AnimationBundle(Animation a1, Animation a2, float delay) {
		this(a1);
		this.add(a2, delay);
	}

	public AnimationBundle(Animation a1, Animation a2) {
		this(a1, a2, 0);
	}

	public AnimationBundle(Animation a1, Animation a2, float delay2, Animation a3, float delay3) {
		this(a1, a2, delay2);
		this.add(a3, delay3);
	}

	public AnimationBundle(Animation a1, Animation a2, Animation a3) {
		this(a1, a2, 0, a3, 0);
	}

	public AnimationBundle add(Animation adding) {
		return this.add(adding, 0);
	}

	public AnimationBundle add(Animation adding, float delay) {
		this.content.add(new Pair<Animation, Float>(adding, delay));
		return this;
	}

	@Override
	public void draw(Graphics g) {
		for (Pair<Animation, Float> ani : this.content) {
			ani.getKey().draw(g);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void poll(Input input, float secounds) {
		for (Pair<Animation, Float> ani : this.content) {
			ani.getKey().poll(input, secounds);
		}
		if (!Float.isNaN(this.time)) {
			this.time += secounds;
			for (Pair<Animation, Float> ani : (ArrayList<Pair<Animation, Float>>) this.toStart.clone()) {
				if (ani.getValue().floatValue() <= this.time) {
					ani.getKey().start(this.color);
					this.toStart.remove(ani);
				}
			}
			if (this.toStart.isEmpty()) {
				this.time = Float.NaN;
			}
		}
	}

	@Override
	public void start(Color color) {
		if (this.fixedColor != null) {
			color = this.fixedColor;
		}
		if (color != null) {
			this.color = color;
		}
		this.time = 0;
		this.toStart.clear();
		for (Pair<Animation, Float> ani : this.content) {
			if (ani.getValue().floatValue() == 0) {
				ani.getKey().start(this.color);
			} else {
				this.toStart.add(ani);
			}
		}
	}

	@Override
	public boolean isRunning() {
		for (Pair<Animation, Float> ani : this.content) {
			if (ani.getKey().isRunning()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Animation setFixedColor(Color color) {
		this.fixedColor = color;
		return this;
	}

}
