package resarcana.game.abilities.specials;

import org.newdawn.slick.util.Log;

import resarcana.game.abilities.Reanimator;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.math.Vector;

public class CreatureReanimator extends Reanimator {

	public static final int MODE_BEAST = 0;
	public static final int MODE_DRAGON = 1;
	public static final int MODE_DEMON = 2;

	private final int mode;

	public CreatureReanimator(Tappable parent, Vector relPos, int mode, EssenceSelection cost) {
		super(parent, relPos, cost);
		this.mode = mode;
	}

	@Override
	public boolean canBeReanimated(Tappable card) {
		switch (this.mode) {
		case MODE_BEAST:
			return super.canBeReanimated(card) && card.isBeast();
		case MODE_DRAGON:
			return super.canBeReanimated(card) && card.isDragon();
		case MODE_DEMON:
			return super.canBeReanimated(card) && card.isDemon();
		default:
			Log.warn("Unknown mode " + this.mode + " for " + this);
			return super.canBeReanimated(card);
		}
	}

}
