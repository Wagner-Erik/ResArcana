package resarcana.game.abilities.specials;

import resarcana.game.abilities.Attack;
import resarcana.game.abilities.Protection;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.math.Vector;

public class ProtectionProducer extends Protection {

	private final EssenceSelection production;

	public ProtectionProducer(Tappable parent, Vector relPos, EssenceSelection cost, boolean tapping,
			EssenceSelection production) {
		super(parent, relPos, cost, tapping);
		this.production = production;
	}

	@Override
	public void protect(Attack attack) {
		super.protect(attack);
		this.getTappable().modifyEssence(this.production, false);
	}
}
