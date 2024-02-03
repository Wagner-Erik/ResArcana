package resarcana.game.abilities;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class Retriever extends Ability implements Selecting {

	private static final int STATE_IDLE = 0;
	private static final int STATE_INPUT = 1;
	private static final int STATE_RETRIEVE = 2;

	public final EssenceSelection cost;

	private int status = STATE_IDLE;

	private EssenceSelection choosenCost;

	public Retriever(Tappable parent, Vector relPos, EssenceSelection cost) {
		super(parent, relPos);
		this.cost = cost;
	}

	@Override
	protected boolean isActivable() {
		return super.isActivable() && this.getPlayer().isPayable(this.cost) && this.getPlayer().getDiscard().size() > 0;
	}

	@Override
	public boolean activate() {
		this.getGameClient()
				.addSelector(new EssenceSelector(this, this.cost, this.getPlayer().getEssenceCounter().getCount(),
						this.cost.getValues(), "Choose cost to retrieve a card"));
		this.status = STATE_INPUT;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(0)), true);
		Artifact target = (Artifact) this.getGame().getTappable(overwrite.getParts().get(1));
		this.getPlayer().retrieve(target);
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
				.setOptionalOne(target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof EssenceSelector && this.status == STATE_INPUT) {
			this.getGameClient().unsetSelector(sel);
			this.choosenCost = ((EssenceSelector) sel).getSelection();
			this.getGameClient()
					.addSelector(new ImageSelector<Artifact>(this, this.getPlayer().getDiscard(), "Retrieve a card"));
			this.status = STATE_RETRIEVE;
		} else if (sel instanceof ImageSelector && this.status == STATE_RETRIEVE) {
			this.getGameClient().unsetSelector(sel);
			Artifact retrieve = ((ImageSelector<Artifact>) sel).getResult();
			this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.choosenCost, retrieve));
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
