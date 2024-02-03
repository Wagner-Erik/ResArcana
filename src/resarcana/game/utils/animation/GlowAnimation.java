package resarcana.game.utils.animation;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

public class GlowAnimation implements Animation {

	private final Rectangle hitbox;
	private final float timeBlink, timeRotation, maxDist;
	private Color backgroundColor;
	private final Color rotatingColor;
	private final Image image;

	private Color curBG, fixedColor;
	private float alpha, phase;
	private Color[] corners = new Color[4];

	public GlowAnimation(Rectangle hitbox, float timeBlink, Color backgroundColor, float timeRotation,
			Color rotatingColor) {
		this.hitbox = hitbox;
		this.maxDist = this.hitbox.getBoundingCircle().radius * 2;
		this.timeBlink = timeBlink;
		this.timeRotation = timeRotation;
		this.backgroundColor = backgroundColor;
		this.rotatingColor = rotatingColor;
		this.image = ResourceManager.getInstance().getImage("misc/glow_raw.png").copy();
	}

	@Override
	public void draw(Graphics g) {
		this.image.draw(this.hitbox.x, this.hitbox.y, this.hitbox.width, this.hitbox.height);
	}

	@Override
	public void poll(Input input, float secounds) {
		this.phase = (float) ((this.phase + secounds % (2 * Math.PI)));
		this.alpha = (float) (0.35 * Math.sin(this.phase / this.timeBlink * 2 * Math.PI) + 0.65);
		Vector pos = this.hitbox.getPositionAtOutline((float) (this.phase / this.timeRotation));
		this.curBG = new Color(this.backgroundColor.r, this.backgroundColor.g, this.backgroundColor.b, this.alpha);
		float dist = pos.sub(this.hitbox.getTopLeftCorner()).abs();
		Color curFG = new Color(this.rotatingColor.r, this.rotatingColor.g, this.rotatingColor.b,
				this.rotatingColor.a * (1 - dist / this.maxDist));
		this.corners[Image.TOP_LEFT] = UtilFunctions.addColors(this.curBG, curFG);
		dist = pos.sub(this.hitbox.getTopRightCorner()).abs();
		curFG = new Color(this.rotatingColor.r, this.rotatingColor.g, this.rotatingColor.b,
				this.rotatingColor.a * (1 - dist / this.maxDist));
		this.corners[Image.TOP_RIGHT] = UtilFunctions.addColors(this.curBG, curFG);
		dist = pos.sub(this.hitbox.getBottomLeftCorner()).abs();
		curFG = new Color(this.rotatingColor.r, this.rotatingColor.g, this.rotatingColor.b,
				this.rotatingColor.a * (1 - dist / this.maxDist));
		this.corners[Image.BOTTOM_LEFT] = UtilFunctions.addColors(this.curBG, curFG);
		dist = pos.sub(this.hitbox.getBottomRightCorner()).abs();
		curFG = new Color(this.rotatingColor.r, this.rotatingColor.g, this.rotatingColor.b,
				this.rotatingColor.a * (1 - dist / this.maxDist));
		this.corners[Image.BOTTOM_RIGHT] = UtilFunctions.addColors(this.curBG, curFG);
		for (int i = 0; i < this.corners.length; i++) {
			this.image.setColor(i, this.corners[i].r, this.corners[i].g, this.corners[i].b, this.corners[i].a);
		}
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
			this.backgroundColor = color;
		}
	}

	@Override
	public boolean isRunning() {
		return true;
	}
}
