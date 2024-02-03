package resarcana.game.utils.animation;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.MaskUtil;

import resarcana.game.utils.animation.generator.PathFactory;
import resarcana.game.utils.animation.generator.PathGenerator;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class ArrowAnimation implements Animation {

	private static final float WIGGLE_RATIO = 0.1f;
	private static final float WIGGLE_START_RATE = 12.5f;

	private final Rectangle outsideBox, imageBox;
	private final String image;
	private final float lifetime, flighttime, maxProgress;
	private final PathGenerator path;

	private Color color = Color.white, fixedColor = null;
	private float progress;
	private boolean autoLooping = false;

	public ArrowAnimation(Rectangle outsideBox, Rectangle imageBox, String image, float verticalPos, float lifetime,
			float flighttime) {
		this.outsideBox = outsideBox;
		this.imageBox = imageBox.modifyCenter(Vector.ZERO);
		this.image = image;
		this.lifetime = lifetime;
		this.flighttime = flighttime;
		this.maxProgress = this.lifetime / this.flighttime;
		this.progress = this.maxProgress + 1;
		this.path = PathFactory.getArcPathBetweenPoints(
				new Vector(outsideBox.getRightEnd() + 5 * imageBox.width, verticalPos),
				new Vector(outsideBox.getLeftEnd(), verticalPos), 0, 0.025f);
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
		if(this.fixedColor != null) {
			color = this.fixedColor;
		}
		if (color != null) {
			this.color = color;
		}
		this.progress = 0;
	}

	@Override
	public boolean isRunning() {
		return this.progress <= this.maxProgress;
	}

	private float getWigglePhase() {
		return WIGGLE_START_RATE / 2 / (1 - this.maxProgress)
				* (this.progress * this.progress - 2 * this.maxProgress * this.progress + 2 * this.maxProgress - 1);
	}

	@Override
	public void draw(Graphics g) {
		if (this.isRunning()) {
			if (this.progress < 1) {
				MaskUtil.defineMask();
				GraphicUtils.fill(g, this.outsideBox);
				MaskUtil.finishDefineMask();
				MaskUtil.drawOnMask();
				g.pushTransform();
				GraphicUtils.translate(g, this.path.getPosition(this.progress));
				g.rotate(0, 0, this.path.getAngle(this.progress));
				float scale = this.path.getScale(this.progress);
				g.scale(scale, scale);
				GraphicUtils.drawImage(g, this.imageBox, ResourceManager.getInstance().getImage(this.image),
						this.color);
				g.popTransform();
				MaskUtil.resetMask();
			} else {
				g.pushTransform();
				GraphicUtils.translate(g, this.path.getPosition(1));
				g.rotate(0, 0, this.path.getAngle(1));
				float wiggle = (float) Math.sin(this.getWigglePhase() * 2 * Math.PI) * this.imageBox.height
						* WIGGLE_RATIO;
				GraphicUtils.drawImageWarped(this.imageBox.getTopLeftCorner().add(-wiggle, 0),
						this.imageBox.getTopRightCorner().add(-wiggle, 0),
						this.imageBox.getBottomRightCorner().add(wiggle, 0),
						this.imageBox.getBottomLeftCorner().add(wiggle, 0),
						ResourceManager.getInstance().getImage(this.image));
				g.popTransform();
			}
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.isRunning()) {
			this.progress += secounds / this.flighttime;
		} else {
			if (this.autoLooping) {
				this.start(this.color);
			}
		}
	}

}
