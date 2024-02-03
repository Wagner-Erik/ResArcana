package resarcana.game.abilities;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Scroll;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class ClaimScroll extends Ability implements Selecting {

	private static final int STATE_IDLE = 0;
	private static final int STATE_INPUT = 1;
	private static final int STATE_SCROLL = 2;

	private final EssenceSelection cost;

	private EssenceSelection choosenCost;

	private int status = STATE_IDLE;

	public ClaimScroll(Tappable parent, Vector relPos, EssenceSelection cost) {
		super(parent, relPos);
		this.cost = cost;
	}

	@Override
	protected boolean isActivable() {
		return super.isActivable() && this.getPlayer().isPayable(this.cost) && this.getGame().getScrolls().size() > 0;
	}

	@Override
	public boolean activate() {
		this.getGameClient()
				.addSelector(new EssenceSelector(this, this.cost, this.getPlayer().getEssenceCounter().getCount(),
						this.cost.getValues(), "Choose cost to claim a scroll"));
		this.status = STATE_INPUT;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(0)), true);
		Scroll scroll = (Scroll) this.getGame().getTappable(overwrite.getParts().get(1));
		this.getPlayer().claimScroll(scroll);
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
				.setOptionalOne(scroll);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof EssenceSelector && this.status == STATE_INPUT) {
			this.getGameClient().unsetSelector(sel);
			this.choosenCost = ((EssenceSelector) sel).getSelection();
			this.getGameClient()
					.addSelector(new ImageSelector<Scroll>(this, this.getGame().getScrolls(), "Claim a scroll"));
			this.status = STATE_SCROLL;
		} else if (sel instanceof ImageSelector && this.status == STATE_SCROLL) {
			this.getGameClient().unsetSelector(sel);
			Scroll scroll = ((ImageSelector<Scroll>) sel).getResult();
			this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.choosenCost, scroll));
			this.status = STATE_IDLE;
		} else {
			Log.warn("Unknown selector " + sel + " for " + this);
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}

}
