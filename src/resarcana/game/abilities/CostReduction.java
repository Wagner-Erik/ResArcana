package resarcana.game.abilities;

import java.util.EnumSet;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class CostReduction extends Ability {

	public static final int MODE_ALL = 0;
	public static final int MODE_DRAGON = 1;
	public static final int MODE_BEAST = 2;
	public static final int MODE_ARTIFACTS = 3;
	public static final int MODE_DEMON = 4;

	private final int mode;
	private final int reductionValue;
	private final EnumSet<Essences> excludes;

	public CostReduction(Tappable parent, Vector relPos, int mode, int value, EnumSet<Essences> excludes) {
		super(parent, relPos);
		this.mode = mode;
		this.reductionValue = value;
		this.excludes = excludes;
	}

	@Override
	public boolean activate() {
		return true; // Passive ability
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		return new HistoryElement(this); // Passive ability
	}

	@Override
	public boolean isActivable() {
		return false;
	}

	public EssenceSelection reduceCost(Tappable card, EssenceSelection costToReduce) {
		switch (this.mode) {
		case MODE_ALL:
			break;
		case MODE_DRAGON:
			if (!card.isDragon()) {
				return costToReduce;
			}
			break;
		case MODE_BEAST:
			if (!card.isBeast()) {
				return costToReduce;
			}
			break;
		case MODE_ARTIFACTS:
			if (!(card instanceof Artifact)) {
				return costToReduce;
			}
			break;
		case MODE_DEMON:
			if (!card.isDemon()) {
				return costToReduce;
			}
			break;
		default:
			Log.warn("Unknown reduction mode " + this.mode + " in " + this);
			break;
		}
		int[] cost = costToReduce.getValues().clone();
		EnumSet<Essences> excl = EnumSet.noneOf(Essences.class);
		int totalRemaining = 0;
		for (Essences ess : Essences.values()) {
			if (costToReduce.isDetermined() && cost[ess.ordinal()] == 0) {
				excl.add(ess);
			} else {
				if (!this.excludes.contains(ess)) { // Reduce if possible
					cost[ess.ordinal()] -= this.reductionValue;
					if (cost[ess.ordinal()] < 0) {
						cost[ess.ordinal()] = 0;
					}
				}
				// Count all determined values after reduction
				totalRemaining += cost[ess.ordinal()];
			}
		}
		// Indetermined value takes the difference between total cost and total
		// remaining determined value minus the reduction value
		cost[cost.length - 1] = costToReduce.getTotal() - totalRemaining - this.reductionValue;
		if (cost[cost.length - 1] < 0) {
			cost[cost.length - 1] = 0;
		}
		return new EssenceSelection(cost, excl);
	}

}
