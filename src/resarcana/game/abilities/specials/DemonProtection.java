package resarcana.game.abilities.specials;

import resarcana.game.abilities.Protection;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.math.Vector;

public class DemonProtection extends Protection {

	public DemonProtection(Tappable parent, Vector relPos, EssenceSelection cost, boolean tapping) {
		super(parent, relPos, cost, tapping);
		this.setEffect(ProtectionEffect.DEMON_SLAYER);
	}

	@Override
	public boolean canProtectFrom(Tappable origin) {
		return super.canProtectFrom(origin) && origin.isDemon();
	}
}
