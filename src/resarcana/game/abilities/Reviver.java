package resarcana.game.abilities;

import java.util.ArrayList;
import java.util.EnumSet;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class Reviver extends Ability implements Selecting {

	public static final int MODE_DISCARD_REVIVE = 0;
	public static final int MODE_DRAGON_REVIVE_ALL = 1;

	private static final int STATE_IDLE = 0;
	private static final int STATE_TARGET = 1;
	private static final int STATE_COST = 2;

	private final int mode;
	private final Essences costType;
	private final int costValue, costReduce;
	private final EnumSet<Essences> exclude;

	private int status = STATE_IDLE;
	private Artifact toRevive = null;

	public Reviver(Tappable parent, Vector relPos, int mode, Essences costType, int costValue, int costReduce,
			EnumSet<Essences> exclude) {
		super(parent, relPos);
		this.mode = mode;
		this.costType = costType;
		this.costValue = costValue;
		this.costReduce = costReduce;
		this.exclude = exclude;
	}

	private EssenceSelection getConvertedCost(EssenceSelection costToConvert) {
		int[] cost = costToConvert.getValues().clone();
		int total = 0;
		for (Essences ess : EnumSet.complementOf(this.exclude)) {
			total += cost[ess.ordinal()];
			cost[ess.ordinal()] = 0;
		}
		if (this.costType == null) {
			total += this.costValue;
		} else {
			cost[this.costType.ordinal()] += this.costValue;
		}
		cost[cost.length - 1] = costToConvert.getIndeterminedValue() + total - this.costReduce;
		if (cost[cost.length - 1] < 0) {
			cost[cost.length - 1] = 0;
		}
		return new EssenceSelection(cost);
	}

	private ArrayList<Artifact> getTargets() {
		ArrayList<Artifact> targets = new ArrayList<Artifact>();
		switch (this.mode) {
		case MODE_DISCARD_REVIVE:
			for (Artifact artifact : this.getPlayer().getDiscard()) {
				if (this.getPlayer().isPayable(this.getConvertedCost(artifact.getCost()))) {
					targets.add(artifact);
				}
			}
			return targets;
		case MODE_DRAGON_REVIVE_ALL:
			for (Artifact artifact : this.getGame().getAllDiscards()) {
				if (artifact.isDragon() && this.getPlayer().isPayable(this.getConvertedCost(artifact.getCost()))) {
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
		return super.isActivable() && this.getTargets().size() > 0;
	}

	@Override
	public boolean activate() {
		ArrayList<Artifact> targets = this.getTargets();
		this.getGameClient().addSelector(new ImageSelector<Artifact>(this, targets, "Select revive target"));
		this.status = STATE_TARGET;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		Artifact target;
		if (overwrite.getParts().size() == 0) {
			target = this.getTargets().get(0);
			this.getPlayer().placeCardFromDiscard(target, this.getConvertedCost(this.toRevive.getCost()));
		} else {
			target = (Artifact) this.getGame().getTappable(overwrite.getParts().get(0));
			this.getPlayer().placeCardFromDiscard(target, new EssenceSelection(overwrite.getParts().get(1)));
		}
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
				.setOptionalOne(target);
	}

	private boolean askCost() {
		EssenceSelection cost = this.getConvertedCost(this.toRevive.getCost());
		if (cost.isDetermined()) {
			this.status = STATE_IDLE;
			return true;
		} else {
			this.getGameClient().addSelector(new EssenceSelector(this, cost,
					this.getPlayer().getEssenceCounter().getCount(), cost.getValues(), "Choose cost for revive"));
			this.status = STATE_COST;
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_TARGET) {
			this.getGameClient().unsetSelector(sel);
			this.toRevive = ((ImageSelector<Artifact>) sel).getResult();
			if (this.askCost()) {
				this.getGame().abilityFinished(this,
						new UserInputOverwrite(this, this.toRevive, this.getConvertedCost(this.toRevive.getCost())));
			}
		} else if (sel instanceof EssenceSelector && this.status == STATE_COST) {
			this.getGameClient().unsetSelector(sel);
			this.getGame().abilityFinished(this,
					new UserInputOverwrite(this, this.toRevive, ((EssenceSelector) sel).getSelection()));
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
