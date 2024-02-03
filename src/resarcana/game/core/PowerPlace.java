package resarcana.game.core;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class PowerPlace extends Tappable {

	public static final Rectangle PLACE_HITBOX = new Rectangle(Vector.ZERO, new Vector(120, 168));
	public static final Vector PLACE_ESSENCES = new Vector(60, -130);

	public final int place_ID;

	private Essences pointEssence = Essences.LIFE;
	private int pointsPerEssence = 0;

	private boolean mouseOver = false;

	public PowerPlace(Game parent, String image, int id) {
		super(parent, image, 1.25f);
		this.place_ID = id;
		this.setAutoCollect(CollectMode.NEVER);
	}

	public void drawBuyHitbox(Graphics g) {
		Color c = g.getColor();
		if (this.mouseOver) {
			g.setColor(Ability.COLOR_SELECTED);
		} else {
			g.setColor(Ability.COLOR_AVAILABLE);
		}
		float lw = g.getLineWidth();
		g.setLineWidth(4);
		GraphicUtils.draw(g, this.getHitbox());
		g.setLineWidth(lw);
		g.setColor(c);
	}

	public void drawBuyGlowing(Graphics g) {
		GraphicUtils.drawImage(g, this.getHitbox(), ResourceManager.getInstance().getImage("misc/glow_raw.png"),
				this.mouseOver ? Ability.COLOR_SELECTED : Ability.COLOR_AVAILABLE, 24, 33.6f);
	}

	public void pollBuyHitbox(Input input, float secounds) {
		this.mouseOver = this.getHitbox()
				.isPointInThis(this.getGame().getCamera().getPosition().add(
						input.getMouseX() / this.getGame().getCamera().getZoom(),
						input.getMouseY() / this.getGame().getCamera().getZoom()));
		if (this.mouseOver) {
			if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON))
				this.getGame().buyPowerPlace(this);
		}
	}

	public void setPointsPerEssence(Essences pointEssence, int pointsPerEssence) {
		if (pointEssence != null) {
			this.pointEssence = pointEssence;
			this.pointsPerEssence = pointsPerEssence;
		} else {
			this.pointEssence = Essences.LIFE;
			this.pointsPerEssence = 0;
		}
	}

	@Override
	public int getPoints() {
		return super.getPoints() + this.getEssenceCount()[this.pointEssence.ordinal()] * this.pointsPerEssence;
	}

	@Override
	public Rectangle getRawHitbox() {
		return PLACE_HITBOX;
	}

	@Override
	public Vector getEssencePosition() {
		return PLACE_ESSENCES;
	}

}
