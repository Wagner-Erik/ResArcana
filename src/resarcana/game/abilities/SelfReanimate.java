package resarcana.game.abilities;

import resarcana.game.core.Ability;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.graphics.utils.SoundManager;
import resarcana.math.Vector;

public class SelfReanimate extends Ability {

	private final EssenceSelection cost;

	public SelfReanimate(Tappable parent, Vector relPos, EssenceSelection cost) {
		super(parent, relPos);
		this.cost = cost;
	}

	@Override
	protected boolean isActivable() {
		return this.getTappable().isTapped() && this.getPlayer().isActive() && this.getTappable().isInPlay()
				&& this.getPlayer().isPayable(this.cost);
	}

	@Override
	public boolean activate() {
		return true;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().untap();
		SoundManager.getInstance().playTap();
		this.getPlayer().modifyEssence(this.cost, true);
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
	}

}
