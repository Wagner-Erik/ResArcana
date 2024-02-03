package resarcana.game.abilities;

import java.util.ArrayList;

import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

public class CardDrawer extends Ability implements Selecting {

	private static final int STATE_IDLE = 0;
	private static final int STATE_INPUT = 1;

	private final EssenceSelection cost;

	private int status = STATE_IDLE;

	public CardDrawer(Tappable parent, Vector relPos, EssenceSelection cost) {
		super(parent, relPos);
		this.cost = cost;
	}

	@Override
	protected boolean isActivable() {
		return super.isActivable() && this.getPlayer().isPayable(this.cost) && this.getPlayer().canDrawCard();
	}

	@Override
	public boolean activate() {
		this.getGameClient().addSelector(new EssenceSelector(this, this.cost,
				this.getPlayer().getEssenceCounter().getCount(), this.cost.getValues(), "Choose cost to draw a card"));
		this.status = STATE_INPUT;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(0)), true);
		if (!this.getPlayer().isActive()) { // Active player has already drawn the card
			this.getPlayer().drawCard((Artifact) this.getGame().getTappable(overwrite.getParts().get(1)));
		}
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
	}

	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof EssenceSelector && this.status == STATE_INPUT) {
			this.getGameClient().unsetSelector(sel);
			EssenceSelection selection = ((EssenceSelector) sel).getSelection();
			ArrayList<Artifact> draw = this.getPlayer().drawTopCards(1);
			this.status = STATE_IDLE;
			this.getGame().abilityFinished(this,
					new UserInputOverwrite(this, selection, UtilFunctions.ListToString(draw)));
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}
}
