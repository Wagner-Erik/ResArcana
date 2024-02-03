package resarcana.game.core;

import org.newdawn.slick.Graphics;

import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class MagicItem extends Tappable {

	public static final Rectangle ITEM_HITBOX = new Rectangle(Vector.ZERO, new Vector(100, 140));
	public static final Vector ITEM_ESSENCES = new Vector(60, -90);

	public MagicItem(Game parent, String image) {
		super(parent, image, 1.f);
	}

	@Override
	public void draw(Graphics g) {
		if (this.getPlayer() != null && this.getPlayer().hasPassed()) {
			GraphicUtils.drawImage(g, this.getHitbox(), ResourceManager.getInstance().getImage("misc/item_back.png"));
		} else {
			super.draw(g);
		}
	}

	@Override
	public Rectangle getRawHitbox() {
		return ITEM_HITBOX;
	}

	@Override
	public Vector getEssencePosition() {
		return ITEM_ESSENCES;
	}

}
