package resarcana.game.abilities;

import java.util.EnumSet;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
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

public class EssenceConverter extends Ability implements Selecting {

	public static final int MODE_ATHANOR = 0;
	public static final int MODE_PHILOSOPHERS_STONE = 1;
	public static final int MODE_PRISM = 2;
	public static final int MODE_PROJECTION_3 = 3;

	private static final int STATE_IDLE = 0;
	private static final int STATE_COST = 1;
	private static final int STATE_INPUT_TYPE = 2;
	private static final int STATE_INPUT_VALUE = 3;
	private static final int STATE_OUTPUT_TYPE = 4;

	private final int mode;
	private final EssenceSelection initialCost;

	private int status = STATE_IDLE;
	private EssenceSelection cost, inputAmount;
	private Essences inputType, outputType;

	public EssenceConverter(Tappable parent, Vector relPos, int mode) {
		super(parent, relPos);
		this.mode = mode;
		switch (this.mode) {
		case MODE_ATHANOR:
			this.initialCost = new EssenceSelection(Essences.ELAN, 6);
			this.cost = this.initialCost;
			this.outputType = Essences.GOLD;
			break;
		case MODE_PHILOSOPHERS_STONE:
			this.initialCost = new EssenceSelection(null, 2);
			this.outputType = Essences.GOLD;
			break;
		case MODE_PRISM:
			this.initialCost = null;
			break;
		case MODE_PROJECTION_3:
			this.initialCost = null;
			this.outputType = Essences.GOLD;
			break;
		default:
			Log.warn("Unknown mode " + this.mode + " for " + this);
			this.initialCost = null;
			break;
		}
	}

	@Override
	protected boolean isActivable() {
		if (super.isActivable()) {
			switch (this.mode) {
			case MODE_ATHANOR:
				return this.getTappable().getEssenceCount()[Essences.ELAN.ordinal()] >= 6;
			case MODE_PHILOSOPHERS_STONE:
				return this.getPlayer().isPayable(this.initialCost);
			case MODE_PRISM:
			case MODE_PROJECTION_3:
				return true;
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
		case MODE_ATHANOR:
			this.chooseInputType();
			break;
		case MODE_PHILOSOPHERS_STONE:
			this.getGameClient().addSelector(
					new EssenceSelector(this, this.initialCost, "Choose initial cost for Philosopher's Stone"));
			this.status = STATE_COST;
			break;
		case MODE_PRISM:
		case MODE_PROJECTION_3:
			this.chooseInputType();
			break;
		default:
			Log.warn("Unknown mode " + this.mode + " for " + this);
			break;
		}
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		switch (this.mode) {
		case MODE_ATHANOR:
			this.getTappable().modifyEssence(this.initialCost, true);
			this.inputAmount = new EssenceSelection(overwrite.getParts().get(1));
			this.getPlayer().modifyEssence(this.inputAmount, true);
			this.getPlayer().modifyEssence(new EssenceSelection(this.outputType, this.inputAmount.getTotal()), false);
			break;
		case MODE_PHILOSOPHERS_STONE:
			this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(0)), true);
			this.inputAmount = new EssenceSelection(overwrite.getParts().get(1));
			this.getPlayer().modifyEssence(this.inputAmount, true);
			this.getPlayer().modifyEssence(new EssenceSelection(this.outputType, this.inputAmount.getTotal()), false);
			break;
		case MODE_PRISM:
		case MODE_PROJECTION_3:
			// No initial cost
			this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(0)), true);
			this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(1)), false);
			break;
		default:
			Log.warn("Unknown mode " + this.mode + " for " + this);
			break;
		}
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
	}

	private void chooseInputType() {
		this.getGameClient().addSelector(new ImageSelector<Essences>(this, Essences.values(),
				"Choose essence to convert with " + this.getTappable().getName()));
		this.status = STATE_INPUT_TYPE;
	}

	private void chooseInputAmount() {
		switch (this.mode) {
		case MODE_ATHANOR:
		case MODE_PHILOSOPHERS_STONE:
		case MODE_PRISM:
			this.getGameClient()
					.addSelector(new EssenceSelector(this,
							new EssenceSelection(EnumSet.complementOf(EnumSet.of(this.inputType))),
							this.getPlayer().getEssenceCounter().getCount(),
							"Choose amount of " + this.inputType.toString() + " to convert", true));
			this.status = STATE_INPUT_VALUE;
			return;
		case MODE_PROJECTION_3:
			int[] maxPayable = this.getPlayer().getEssenceCounter().getCount();
			// Need three input essences for one GOLD output
			// Only this one entry will be needed, all others are excluded anyway
			maxPayable[this.outputType.ordinal()] = maxPayable[this.inputType.ordinal()] / 3;
			this.getGameClient().addSelector(new EssenceSelector(this,
					new EssenceSelection(EnumSet.complementOf(EnumSet.of(this.outputType))), maxPayable,
					"Choose amount of GOLD to create (pay x3 " + this.inputType.toString() + " per GOLD)", true));
			this.status = STATE_INPUT_VALUE;
			return;
		default:
			Log.warn("Unknown mode " + this.mode + " for " + this);
			return;
		}
	}

	private void chooseOutputType() {
		this.getGameClient()
				.addSelector(new ImageSelector<Essences>(this,
						EnumSet.complementOf(EnumSet.of(Essences.GOLD))
								.toArray(new Essences[Essences.values().length - 1]),
						"Choose essence to convert to with " + this.getTappable().getName()));
		this.status = STATE_OUTPUT_TYPE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof EssenceSelector && this.status == STATE_COST) {
			this.getGameClient().unsetSelector(sel);
			this.cost = ((EssenceSelector) sel).getSelection();
			this.chooseInputType();
		} else if (sel instanceof ImageSelector && this.status == STATE_INPUT_TYPE) {
			this.getGameClient().unsetSelector(sel);
			this.inputType = ((ImageSelector<Essences>) sel).getResult();
			this.chooseInputAmount();
		} else if (sel instanceof EssenceSelector && this.status == STATE_INPUT_VALUE) {
			this.getGameClient().unsetSelector(sel);
			this.inputAmount = ((EssenceSelector) sel).getSelection();
			switch (this.mode) {
			case MODE_ATHANOR:
			case MODE_PHILOSOPHERS_STONE:
				this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.cost, this.inputAmount));
				this.status = STATE_IDLE;
				return;
			case MODE_PRISM:
				this.chooseOutputType();
				return;
			case MODE_PROJECTION_3:
				this.getGame().abilityFinished(this,
						new UserInputOverwrite(this,
								new EssenceSelection(this.inputType, this.inputAmount.getTotal() * 3),
								new EssenceSelection(this.outputType, this.inputAmount.getTotal())));
				this.status = STATE_IDLE;
				return;
			default:
				Log.info("Unknown mode " + this.mode + " for " + this);
				this.status = STATE_IDLE;
				return;
			}
		} else if (sel instanceof ImageSelector && this.status == STATE_OUTPUT_TYPE) {
			this.getGameClient().unsetSelector(sel);
			switch (this.mode) {
			case MODE_ATHANOR:
			case MODE_PHILOSOPHERS_STONE:
				Log.warn("State OUTPUT_TYPE reached in wrong mode");
				this.status = STATE_IDLE;
				return;
			case MODE_PRISM:
				this.outputType = ((ImageSelector<Essences>) sel).getResult();
				EssenceSelection outputAmount = new EssenceSelection(this.outputType,
						this.inputAmount.getValue(this.inputType));
				this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.inputAmount, outputAmount));
				this.status = STATE_IDLE;
				return;
			default:
				Log.warn("Unknown mode " + this.mode + " for " + this);
				this.status = STATE_IDLE;
				return;
			}
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
