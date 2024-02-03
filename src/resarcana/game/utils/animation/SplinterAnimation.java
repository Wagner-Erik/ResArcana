package resarcana.game.utils.animation;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.game.utils.animation.generator.PathGenerator;
import resarcana.game.utils.animation.generator.WarpGenerator;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Distributor;

public class SplinterAnimation implements Animation {

	private final Rectangle imageBox;
	private final Distributor<WarpGenerator> generators;
	private final WarpGenerator globalWarp;
	private final PathGenerator path;
	private final String image;
	private final float lifetime;
	private final int rows, cols;

	private Color color = Color.white, fixedColor = null;
	private float progress;
	private boolean autoLooping = false;

	public SplinterAnimation(Rectangle imageBox, PathGenerator path, String image, WarpGenerator globalWarp,
			Distributor<WarpGenerator> generators, int rows, int cols, float lifetime) {
		this.imageBox = imageBox.modifyCenter(Vector.ZERO);
		this.path = path;
		this.image = image;
		this.globalWarp = globalWarp;
		this.generators = generators;
		this.rows = rows;
		this.cols = cols;
		this.lifetime = lifetime;

		this.progress = 2;
	}

	public void setAutolooping(boolean autoLooping) {
		this.autoLooping = autoLooping;
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
		}
		this.progress = 0;
	}

	@Override
	public boolean isRunning() {
		return this.progress <= 1;
	}

	@Override
	public void draw(Graphics g) {
		if (this.isRunning()) {
			g.pushTransform();
			GraphicUtils.translate(g, this.path.getPosition(this.progress));
			g.rotate(0, 0, this.path.getAngle(this.progress));
			float scale = this.path.getScale(this.progress);
			g.scale(scale, scale);
			ResourceManager.getInstance().getImage(this.image).drawMultiQuads(this.imageBox, this.rows, this.cols, globalWarp,
					this.generators, this.progress, this.color);
			g.popTransform();
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.isRunning()) {
			this.progress += secounds / this.lifetime;
		} else {
			if (this.autoLooping) {
				this.start(this.color);
			}
		}
	}

}
