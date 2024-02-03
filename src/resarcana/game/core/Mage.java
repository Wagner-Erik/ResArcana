package resarcana.game.core;

import org.newdawn.slick.Graphics;

import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class Mage extends Tappable {

	public static final Rectangle MAGE_HITBOX = new Rectangle(Vector.ZERO, new Vector(100, 140));
	public static final Vector MAGE_ESSENCES = new Vector(60, -45);

	public static final Vector POSITION_DISCARD_ALL = new Vector(80, 160);
	public static final Vector POSITION_DISCARD_GOLD = new Vector(-80, 160);

	public Mage(Game parent, String image) {
		super(parent, image, 1.f);
	}

	@Override
	public void draw(Graphics g) {
		GraphicUtils.drawImage(g, new Rectangle(this.getPosition().add(POSITION_DISCARD_ALL), Ability.ABILITY_HITBOX),
				ResourceManager.getInstance().getImage("misc/discard_all.png"));
		GraphicUtils.drawImage(g, new Rectangle(this.getPosition().add(POSITION_DISCARD_GOLD), Ability.ABILITY_HITBOX),
				ResourceManager.getInstance().getImage("misc/discard_gold.png"));
		super.draw(g);
	}

	@Override
	public Rectangle getRawHitbox() {
		return MAGE_HITBOX;
	}

	@Override
	public Vector getEssencePosition() {
		return MAGE_ESSENCES;
	}

}
