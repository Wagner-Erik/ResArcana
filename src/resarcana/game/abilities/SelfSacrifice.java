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

public class SelfSacrifice extends Ability implements Selecting {

	public static final int MODE_FLAT = 0;
	public static final int MODE_DISCARD = 1;
	public static final int MODE_DRAGON_EGG = 2;

	public static final int STATE_IDLE = 0;
	public static final int STATE_SELECTING = 1;
	public static final int STATE_OUTPUT = 2;

	private final EssenceSelection reward;
	private final int mode;
	private final int value;
	private final Essences exclude;

	private Artifact selected;

	private int status = STATE_IDLE;

	public SelfSacrifice(Tappable parent, Vector relPos, EssenceSelection reward) {
		super(parent, relPos);
		this.reward = reward;
		this.mode = MODE_FLAT;
		this.value = 0;
		this.exclude = null;
		if (!(parent instanceof Artifact)) {
			Log.warn("Creating SelfSacrifice for non Artifact");
		}
	}

	public SelfSacrifice(Tappable parent, Vector relPos, int mode, int value, Essences exclude) {
		super(parent, relPos);
		this.reward = null;
		this.mode = mode;
		this.value = value;
		this.exclude = exclude;
		if (!(parent instanceof Artifact)) {
			Log.warn("Creating SelfSacrifice for non Artifact");
		}
	}

	@Override
	protected boolean isActivable() {
		if (super.isActivable()) {
			switch (this.mode) {
			case MODE_FLAT:
				return true;
			case MODE_DISCARD:
				return this.getPlayer().getHand().size() > 0;
			case MODE_DRAGON_EGG:
				return this.getDragons(this.getPlayer().getHand()).size() > 0;
			default:
				Log.warn("Unknown mode " + this.mode + " for " + this);
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean activate() {
		switch (this.mode) {
		case MODE_FLAT:
			return true;
		case MODE_DISCARD:
			this.getGameClient()
					.addSelector(new ImageSelector<Artifact>(this, this.getPlayer().getHand(), "Discard artifact"));
			this.status = STATE_SELECTING;
			return false;
		case MODE_DRAGON_EGG:
			this.getGameClient().addSelector(new ImageSelector<Artifact>(this,
					this.getDragons(this.getPlayer().getHand()), "Play dragon for " + this.value + " less"));
			this.status = STATE_SELECTING;
			return false;
		default:
			Log.warn("Unknown mode " + this.mode + " for " + this);
			return false;
		}
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		Artifact target;
		switch (this.mode) {
		case MODE_FLAT:
			this.getPlayer().destroyArtifact((Artifact) this.getTappable());
			this.getPlayer().modifyEssence(this.reward, false);
			return new HistoryElement(this)
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
		case MODE_DISCARD:
			this.getPlayer().destroyArtifact((Artifact) this.getTappable());
			target = (Artifact) this.getGame().getTappable(overwrite.getParts().get(0));
			this.getPlayer().discardCard(target);
			this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(1)), false);
			return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
					.setOptionalOne(target);
		case MODE_DRAGON_EGG:
			this.getPlayer().destroyArtifact((Artifact) this.getTappable());
			target = (Artifact) this.getGame().getTappable(overwrite.getParts().get(0));
			this.getPlayer().placeCardFromHand(target, new EssenceSelection(overwrite.getParts().get(1)));
			return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
					.setOptionalOne(target);
		default:
			Log.warn("Unknown mode " + this.mode + " for " + this);
			return new HistoryElement(this)
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_SELECTING) {
			this.getGameClient().unsetSelector(sel);
			this.selected = ((ImageSelector<Artifact>) sel).getResult();
			switch (this.mode) {
			case MODE_FLAT:
				Log.warn("Unexpected selector " + sel + " in MODE_FLAT for " + this);
				this.status = STATE_IDLE;
				return;
			case MODE_DISCARD:
				this.getGameClient()
						.addSelector(new EssenceSelector(this,
								new EssenceSelection(this.selected.getRawCost().getTotal() + this.value, this.exclude),
								"Choose reward"));
				this.status = STATE_OUTPUT;
				return;
			case MODE_DRAGON_EGG:
				this.getGameClient().addSelector(new EssenceSelector(this,
						this.reduceDragonCost(this.selected.getCost(), this.value), "Choose cost of dragon"));
				this.status = STATE_OUTPUT;
				return;
			default:
				Log.warn("Unknown mode " + this.mode + " for " + this);
				return;
			}
		} else if (sel instanceof EssenceSelector && this.status == STATE_OUTPUT) {
			this.getGameClient().unsetSelector(sel);
			EssenceSelection selection = ((EssenceSelector) sel).getSelection();
			switch (this.mode) {
			case MODE_FLAT:
				Log.warn("Unexpected selector " + sel + " in MODE_FLAT for " + this);
				this.status = STATE_IDLE;
				return;
			case MODE_DISCARD:
				this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.selected, selection));
				this.status = STATE_IDLE;
				return;
			case MODE_DRAGON_EGG:
				this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.selected, selection));
				this.status = STATE_IDLE;
				return;
			default:
				Log.warn("Unknown mode " + this.mode + " for " + this);
				return;
			}
		} else {
			Log.warn("Unexpected selector " + sel + " for " + this);
		}
	}

	private ArrayList<Artifact> getDragons(ArrayList<Artifact> list) {
		ArrayList<Artifact> out = new ArrayList<Artifact>();
		for (Artifact artifact : list) {
			if (artifact.isDragon()
					&& this.getPlayer().isPayable(this.reduceDragonCost(artifact.getCost(), this.value))) {
				out.add(artifact);
			}
		}
		return out;
	}

	private EssenceSelection reduceDragonCost(EssenceSelection costToReduce, int reductionValue) {
		int[] cost = costToReduce.getValues().clone();
		EnumSet<Essences> excl = EnumSet.noneOf(Essences.class);
		int totalRemaining = 0;
		for (Essences ess : Essences.values()) {
			if (costToReduce.isDetermined() && cost[ess.ordinal()] == 0) {
				excl.add(ess);
			} else {
				cost[ess.ordinal()] -= reductionValue;
				if (cost[ess.ordinal()] < 0) {
					cost[ess.ordinal()] = 0;
				}
				totalRemaining += cost[ess.ordinal()];
			}
		}
		cost[cost.length - 1] = costToReduce.getTotal() - totalRemaining - reductionValue;
		if (cost[cost.length - 1] < 0) {
			cost[cost.length - 1] = 0;
		}
		return new EssenceSelection(cost, excl);
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}
}
