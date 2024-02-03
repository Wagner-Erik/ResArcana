package resarcana.game.utils.animation;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.MaskUtil;

import resarcana.game.utils.animation.generator.PathGenerator;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class SweepAnimation implements Animation {

	private final Rectangle outsideBox, imageBox;
	private final PathGenerator path;
	private final String image;
	private final float lifetime;

	private Color color = Color.white, fixedColor = null;
	private float progress;
	private boolean autoLooping = false;

	public SweepAnimation(Rectangle outsideBox, Rectangle imageBox, PathGenerator path, String image, float lifetime) {
		this.outsideBox = outsideBox;
		this.imageBox = imageBox.modifyCenter(Vector.ZERO);
		this.path = path;
		this.image = image;
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
			if (this.outsideBox != null) {
				MaskUtil.defineMask();
				GraphicUtils.fill(g, this.outsideBox);
				MaskUtil.finishDefineMask();
				MaskUtil.drawOnMask();
			}
			g.pushTransform();
			GraphicUtils.translate(g, this.path.getPosition(this.progress));
			g.rotate(0, 0, this.path.getAngle(this.progress));
			float scale = this.path.getScale(this.progress);
			g.scale(scale, scale);
			GraphicUtils.drawImage(g, this.imageBox, ResourceManager.getInstance().getImage(this.image), this.color);
			g.popTransform();
			MaskUtil.resetMask();
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
