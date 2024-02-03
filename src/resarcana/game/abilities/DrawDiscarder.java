package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
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
import resarcana.utils.UtilFunctions;

public class DrawDiscarder extends Ability implements Selecting {

	private static final int STATE_IDLE = 0;
	private static final int STATE_DRAW = 1;
	private static final int STATE_DISCARD = 2;

	private final int draw, discard;
	private final EssenceSelection cost;

	private int status;
	private ArrayList<Artifact> discarded = new ArrayList<Artifact>(), draws;
	private EssenceSelection selectedCost;

	public DrawDiscarder(Tappable parent, Vector relPos, int draw, int discard, EssenceSelection cost) {
		super(parent, relPos);
		this.draw = draw;
		this.discard = discard;
		this.cost = cost;
	}

	public DrawDiscarder(Tappable parent, Vector relPos, int amount) {
		this(parent, relPos, amount, amount, new EssenceSelection());
	}

	@Override
	protected boolean isActivable() {
		return super.isActivable() && this.getPlayer().canDrawCard() && this.getPlayer().isPayable(this.cost);
	}

	@Override
	public boolean activate() {
		this.getGameClient().addSelector(new EssenceSelector(this, this.cost, "Draw cards?"));
		this.status = STATE_DRAW;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(0)), true);
		if (!this.getPlayer().isActive()) { // Player will still be active, dont redraw cards
			ArrayList<Tappable> draws = UtilFunctions.StringArrayToTappables(
					overwrite.getParts().get(1).split(CommunicationKeys.SEPERATOR_VALUES), this.getGame());
			for (Tappable tappable : draws) {
				this.getPlayer().drawCard((Artifact) tappable);
			}
		}
		switch (overwrite.getParts().size()) {
		case 3:
			ArrayList<Tappable> discard = UtilFunctions.StringArrayToTappables(
					overwrite.getParts().get(2).split(CommunicationKeys.SEPERATOR_VALUES), this.getGame());
			for (Tappable tappable : discard) {
				this.getPlayer().discardCard((Artifact) tappable);
			}
			return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
					.setOptionalOne(discard.get(0));
		case 2:
		default:
			return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof EssenceSelector && this.status == STATE_DRAW) {
			this.getGameClient().unsetSelector(sel);
			this.selectedCost = ((EssenceSelector) sel).getSelection();
			this.draws = this.getPlayer().drawTopCards(this.draw);
			if (this.getPlayer().getHand().size() > this.draw - this.discard) {
				this.discarded.clear();
				this.getGameClient()
						.addSelector(new ImageSelector<Artifact>(this, this.getPlayer().getHand(),
								"Discard artifact (" + (this.discarded.size() + 1) + " of "
										+ Math.min(this.discard,
												this.getPlayer().getHand().size() - (this.draw - this.discard))
										+ ")").disableCancel());
				this.status = STATE_DISCARD;
			} else {
				this.getGame().abilityFinished(this,
						new UserInputOverwrite(this, this.selectedCost, UtilFunctions.ListToString(this.draws)));
				this.status = STATE_IDLE;
			}
		} else if (sel instanceof ImageSelector && this.status == STATE_DISCARD) {
			this.getGameClient().unsetSelector(sel);
			this.discarded.add(((ImageSelector<Artifact>) sel).getResult());
			ArrayList<Artifact> hand = this.getPlayer().getHand();
			hand.removeAll(this.discarded);
			if (this.discarded.size() == this.discard || hand.size() == this.draw - this.discard) {
				this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.selectedCost,
						UtilFunctions.ListToString(this.draws), UtilFunctions.ListToString(this.discarded)));
				this.status = STATE_IDLE;
			} else {
				this.getGameClient()
						.addSelector(new ImageSelector<Artifact>(this, hand,
								"Discard artifact (" + (this.discarded.size() + 1) + " of "
										+ Math.min(this.draw, this.getPlayer().getHand().size()) + ")")
												.disableCancel());
				this.status = STATE_DISCARD;
			}
			return;
		} else {
			Log.warn("Unknown selector " + sel + " for " + this);
			return;
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}
}
