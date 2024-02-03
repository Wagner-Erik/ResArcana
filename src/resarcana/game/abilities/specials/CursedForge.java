package resarcana.game.abilities.specials;

import java.util.ArrayList;

import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.PowerPlace;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.userinput.ImageHolder;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;

public class CursedForge extends PowerPlace {

	private static final int STATE_IDLE = 0;
	private static final int STATE_INCOME = 1;

	private ArrayList<ImageHolder> incomeSel;

	private int state = STATE_IDLE;

	public CursedForge(Game parent) {
		super(parent, "place/place_03.png", 3);
		this.setName("Cursed Forge");
		this.setRawCosts(new EssenceSelection(Essences.ELAN, 6, Essences.DEATH, 3));
		this.setPoints(1);
		this.setPointsPerEssence(Essences.GOLD, 1);
		this.incomeSel = new ArrayList<ImageHolder>();
		this.incomeSel.add(Essences.DEATH);
		this.incomeSel.add(this);
	}

	@Override
	protected boolean askIncome(boolean collecting) {
		if (this.getPlayer().getEssenceCounter().isPayable(new EssenceSelection(Essences.DEATH, 1))) {
			this.getGameClient().addSelector(new ImageSelector<ImageHolder>(this, this.incomeSel,
					"Pay one " + Essences.DEATH + " or tap " + this.getName()).disableCancel());
			this.state = STATE_INCOME;
			return false;
		} else {
			this.tap();
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.state == STATE_INCOME) {
			this.getGameClient().unsetSelector(sel);
			ImageHolder result = ((ImageSelector<ImageHolder>) sel).getResult();
			if (result == Essences.DEATH) {
				this.getPlayer().incomeFinished(this, new UserInputOverwrite(this, "Death"));
			} else if (result == this) {
				this.getPlayer().incomeFinished(this, new UserInputOverwrite(this, "Tap"));
			}
		} else {
			super.processSelection(sel);
		}
	}

	@Override
	public void userAction(UserInputOverwrite action) {
		if (action.getParts().get(0).equalsIgnoreCase("Death")) {
			this.getPlayer().modifyEssence(new EssenceSelection(Essences.DEATH, 1), true);
		} else if (action.getParts().get(0).equalsIgnoreCase("Tap")) {
			this.tap();
		} else {
			super.userAction(action);
		}
	}
}
