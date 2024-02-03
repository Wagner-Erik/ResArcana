package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Tappable;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class EssenceCollector extends Ability implements Selecting {

	private static final int STATE_IDLE = 0;
	private static final int STATE_TARGET = 1;

	private int status = STATE_IDLE;

	public EssenceCollector(Tappable parent, Vector relPos) {
		super(parent, relPos);
	}

	public ArrayList<Tappable> getTargets() {
		ArrayList<Tappable> targets = this.getPlayer().getTappablesInPlay();
		int i = 0;
		while (i < targets.size()) {
			if (targets.get(i).hasEssences()) {
				i++;
			} else {
				targets.remove(i);
			}
		}
		return targets;
	}

	@Override
	protected boolean isActivable() {
		return super.isActivable() && this.getTargets().size() > 0;
	}

	@Override
	public boolean activate() {
		this.getGameClient().addSelector(
				new ImageSelector<Tappable>(this, this.getTargets(), "Select card to collect all essences from"));
		this.status = STATE_TARGET;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		Tappable target = this.getGame().getTappable(overwrite.getParts().get(0));
		target.collectEssences();
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
				.setOptionalOne(target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_TARGET) {
			this.getGameClient().unsetSelector(sel);
			this.getGame().abilityFinished(this,
					new UserInputOverwrite(this, ((ImageSelector<Tappable>) sel).getResult()));
			this.status = STATE_IDLE;
		} else {
			Log.warn("Unexpected selector " + sel + " for " + this);
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}

}
