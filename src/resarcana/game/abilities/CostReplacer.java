package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class CostReplacer extends Ability implements Selecting {

	public static final int MODE_DRAGON = 0;

	private static final int STATE_IDLE = 0;
	private static final int STATE_TARGET = 1;

	private final int mode;
	private final EssenceSelection cost;

	private int status = STATE_IDLE;

	public CostReplacer(Tappable parent, Vector relPos, int mode, EssenceSelection cost) {
		super(parent, relPos);
		this.cost = cost;
		this.mode = mode;
	}

	private ArrayList<Artifact> getTargets() {
		ArrayList<Artifact> targets = new ArrayList<Artifact>();
		switch (this.mode) {
		case MODE_DRAGON:
			for (Artifact artifact : this.getPlayer().getHand()) {
				if (artifact.isDragon()) {
					targets.add(artifact);
				}
			}
			return targets;
		default:
			Log.warn("Unknwon mode " + this.mode + " for " + this);
			return new ArrayList<Artifact>();
		}
	}

	@Override
	protected boolean isActivable() {
		return super.isActivable() && this.getPlayer().isPayable(this.cost) && this.getTargets().size() > 0;
	}

	@Override
	public boolean activate() {
		ArrayList<Artifact> targets = this.getTargets();
		this.getGameClient().addSelector(new ImageSelector<Artifact>(this, targets, "Select target to play"));
		this.status = STATE_TARGET;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		Artifact target;
		if (overwrite.getParts().size() == 0) {
			target = this.getTargets().get(0);
			this.getPlayer().placeCardFromHand(target, this.cost);
		} else {
			target = (Artifact) this.getGame().getTappable(overwrite.getParts().get(0));
			this.getPlayer().placeCardFromHand(target,
					this.cost);
		}
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
				.setOptionalOne(target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_TARGET) {
			this.getGameClient().unsetSelector(sel);
			Artifact result = ((ImageSelector<Artifact>) sel).getResult();
			this.getGame().abilityFinished(this, new UserInputOverwrite(this, result));
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
