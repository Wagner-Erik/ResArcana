package resarcana.game.core;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class Monument extends Tappable {

	public static final Rectangle MONUMENT_HITBOX = new Rectangle(Vector.ZERO, new Vector(100, 140));
	public static final Vector MONUMENT_ESSENCES = new Vector(60, -90);

	private boolean mouseOver = false;

	public Monument(Game parent, String image) {
		super(parent, image, 1.f);
	}

	public void drawCardBack(Graphics g) {
		GraphicUtils.drawImage(g, this.getHitbox(), ResourceManager.getInstance().getImage("misc/monument_back.png"));
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
				this.mouseOver ? Ability.COLOR_SELECTED : Ability.COLOR_AVAILABLE, 20, 25);
	}

	public void pollBuyHitbox(Input input, float secounds) {
		this.mouseOver = this.getHitbox()
				.isPointInThis(this.getGame().getCamera().getPosition().add(
						input.getMouseX() / this.getGame().getCamera().getZoom(),
						input.getMouseY() / this.getGame().getCamera().getZoom()));
		if (this.mouseOver) {
			if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON))
				this.getGame().buyMonument(this);
		}
	}

	@Override
	public Rectangle getRawHitbox() {
		return MONUMENT_HITBOX;
	}

	@Override
	public Vector getEssencePosition() {
		return MONUMENT_ESSENCES;
	}

}
