package resarcana.game.abilities;

import java.util.ArrayList;

import resarcana.game.core.Ability;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class Reanimator extends Ability implements Selecting {

	private static final int STATE_IDLE = 0;
	private static final int STATE_INPUT = 1;
	private static final int STATE_TAPPABLE = 2;

	private final EssenceSelection cost;
	private boolean reanimateAll = false;

	private int status = STATE_IDLE;

	private EssenceSelection storedInput;

	public Reanimator(Tappable parent, Vector relPos, EssenceSelection cost) {
		super(parent, relPos);
		this.cost = cost;
	}

	public Reanimator makeReanimateAll() {
		this.reanimateAll = true;
		return this;
	}

	private ArrayList<Tappable> getTargets() {
		ArrayList<Tappable> tappables;
		if (this.reanimateAll) {
			tappables = this.getGame().getAllTappablesInPlay();
		} else {
			tappables = this.getPlayer().getTappablesInPlay();
		}
		int i = 0;
		while (i < tappables.size()) {
			if (this.canBeReanimated(tappables.get(i))) { // keep untapped
				i++;
			} else { // remove non tapped or non fitting types
				tappables.remove(i);
			}
		}
		return tappables;
	}

	public boolean canBeReanimated(Tappable card) {
		return card.isTapped();
	}

	@Override
	protected boolean isActivable() {
		if (this.getPlayer() == null) {
			return false;
		} else {
			return super.isActivable() && this.getPlayer().isPayable(this.cost) && this.getTargets().size() > 0;
		}
	}

	@Override
	public boolean activate() {
		if (this.cost.isDetermined()) {
			this.storedInput = this.cost;
			if (this.reanimateAll) {
				return true;
			} else {
				this.askUntap();
				return false;
			}
		} else {
			this.getGameClient()
					.addSelector(new EssenceSelector(this, this.cost, this.getPlayer().getEssenceCounter().getCount(),
							this.cost.getValues(), "Cost for " + this.getTappable().getName()));
			this.status = STATE_INPUT;
			return false;
		}
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(0)), true);
		if (this.reanimateAll) {
			ArrayList<Tappable> targets = this.getTargets();
			for (Tappable toReanimate : targets) {
				toReanimate.untap();
			}
			if (targets.size() > 0) {
				return new HistoryElement(this)
						.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
						.setOptionalOne(targets.get(0));
			} else {
				return new HistoryElement(this)
						.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
			}
		} else {
			Tappable target = this.getGame().getTappable(overwrite.getParts().get(1));
			target.untap();
			return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
					.setOptionalOne(target);
		}
	}

	private void askUntap() {
		this.getGameClient().addSelector(new ImageSelector<Tappable>(this, this.getTargets(), "Select card to untap"));
		this.status = STATE_TAPPABLE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof EssenceSelector && this.status == STATE_INPUT) {
			this.getGameClient().unsetSelector(sel);
			this.storedInput = ((EssenceSelector) sel).getSelection();
			if (this.reanimateAll) {
				this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.storedInput));
			} else {
				this.askUntap();
			}
		} else if (sel instanceof ImageSelector && this.status == STATE_TAPPABLE) {
			this.getGameClient().unsetSelector(sel);
			this.status = STATE_IDLE;
			this.getGame().abilityFinished(this,
					new UserInputOverwrite(this, this.storedInput, ((ImageSelector<Tappable>) sel).getResult()));
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}
}
