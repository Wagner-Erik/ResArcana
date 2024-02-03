package resarcana.game.abilities.specials;

import resarcana.game.abilities.Protection;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.math.Vector;

public class DragonProtection extends Protection {

	public DragonProtection(Tappable parent, Vector relPos, EssenceSelection cost, boolean tapping) {
		super(parent, relPos, cost, tapping);
	}

	@Override
	public boolean canProtectFrom(Tappable origin) {
		return super.canProtectFrom(origin) && origin.isDragon();
	}
}
