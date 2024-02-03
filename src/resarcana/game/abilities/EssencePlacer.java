package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Essences;
import resarcana.game.core.MagicItem;
import resarcana.game.core.Scroll;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class EssencePlacer extends Ability implements Selecting {

	private static final int STATE_IDLE = 0;
	private static final int STATE_INPUT = 1;
	private static final int STATE_OUTPUT = 2;
	private static final int STATE_TAPPABLE = 3;

	public static final int MODE_PLAYER = 10;
	public static final int MODE_SELF = 11;
	public static final int MODE_IN_PLAY = 12;

	public static final int OUTPUT_INDEPENDENT = 100;
	public static final int OUTPUT_AS_INPUT = 101;

	public final EssenceSelection inputRequest, outputRequest, allOutput;
	public final boolean tapping;
	public final int mode, outputMode;

	private int status = STATE_IDLE;

	private EssenceSelection storedOutput = null, storedInput = null;
	private Tappable result;

	public EssencePlacer(Tappable parent, Vector relPos, int mode, boolean tapping, EssenceSelection input,
			EssenceSelection output, Essences all, int outputMode) {
		super(parent, relPos);
		this.mode = mode;
		this.outputMode = outputMode;
		this.tapping = tapping;
		this.inputRequest = input;
		this.outputRequest = output;
		if (this.mode == MODE_PLAYER) {
			this.allOutput = new EssenceSelection(all, 1);
		} else {
			this.allOutput = null;
		}
	}

	public EssencePlacer(Tappable parent, Vector relPos, int mode, boolean tapping, EssenceSelection input,
			EssenceSelection output, Essences all) {
		this(parent, relPos, mode, tapping, input, output, all, OUTPUT_INDEPENDENT);
	}

	public EssencePlacer(Tappable parent, Vector relPos, int mode, boolean tapping, EssenceSelection input,
			EssenceSelection output, int outputMode) {
		this(parent, relPos, mode, tapping, input, output, null, outputMode);
	}

	public EssencePlacer(Tappable parent, Vector relPos, int mode, boolean tapping, EssenceSelection input,
			EssenceSelection output) {
		this(parent, relPos, mode, tapping, input, output, null, OUTPUT_INDEPENDENT);
	}

	@Override
	protected boolean isActivable() {
		return super.isActivable() && this.getPlayer().isPayable(this.inputRequest);
	}

	private boolean processInput() {
		if (!this.inputRequest.isDetermined()) {
			this.getGameClient()
					.addSelector(new EssenceSelector(this, this.inputRequest,
							this.getPlayer().getEssenceCounter().getCount(),
							"Choose cost for " + this.getTappable().getName(), this.inputRequest.getTotal() < 0));
			this.status = STATE_INPUT;
			return false;
		} else {
			this.storedInput = this.inputRequest;
			return true;
		}
	}

	private boolean processOutput() {
		switch (this.outputMode) {
		case OUTPUT_INDEPENDENT:
			if (this.outputRequest.isDetermined()) {
				this.storedOutput = this.outputRequest;
				switch (this.mode) {
				case MODE_PLAYER:
					return true;
				case MODE_SELF:
					return true;
				case MODE_IN_PLAY:
					this.getGameClient().addSelector(new ImageSelector<Tappable>(this,
							this.getPlayer().getTappablesInPlay(), "Select card to place essence on"));
					this.status = STATE_TAPPABLE;
					return false;
				default:
					Log.warn("Invalid mode (" + this.mode + ") for " + this);
					return true;
				}
			} else {
				this.getGameClient().addSelector(new EssenceSelector(this, this.outputRequest,
						"Choose output for " + this.getTappable().getName()));
				this.status = STATE_OUTPUT;
				return false;
			}
		case OUTPUT_AS_INPUT:
			this.storedOutput = this.storedInput;
			switch (this.mode) {
			case MODE_PLAYER:
				return true;
			case MODE_SELF:
				return true;
			case MODE_IN_PLAY:
				this.getGameClient().addSelector(new ImageSelector<Tappable>(this,
						this.getPlayer().getTappablesInPlay(), "Select card to place essence on"));
				this.status = STATE_TAPPABLE;
				return false;
			default:
				Log.warn("Invalid mode (" + this.mode + ") for " + this);
				return true;
			}
		default:
			Log.warn("Unknown outputMode " + this.outputMode + " in " + this);
			return true;
		}
	}

	@Override
	public boolean activate() {
		if (this.processInput()) {
			return this.processOutput();
		} else {
			return false;
		}
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		if (this.tapping) {
			this.getTappable().tap();
		}
		if (overwrite.getParts().size() == 0) {
			this.getPlayer().modifyEssence(this.inputRequest, true);
			switch (this.mode) {
			case MODE_PLAYER:
				this.getPlayer().modifyEssence(this.outputRequest, false);
				this.getGame().giveEssenceToAll(this.allOutput);
				break;
			case MODE_SELF:
				this.getTappable().modifyEssence(this.outputRequest, false);
				break;
			default:
				Log.error("Mismatching UserInputOverwrite (" + overwrite + ") for " + this);
				break;
			}
			return new HistoryElement(this)
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
		} else if (overwrite.getParts().size() == 3) {
			this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(0)), true);
			switch (this.mode) {
			case MODE_PLAYER:
				this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(1)), false);
				this.getGame().giveEssenceToAll(this.allOutput);
				break;
			case MODE_SELF:
				this.getTappable().modifyEssence(new EssenceSelection(overwrite.getParts().get(1)), false);
				break;
			case MODE_IN_PLAY:
				Tappable target = this.getGame().getTappable(overwrite.getParts().get(2));
				target.modifyEssence(new EssenceSelection(overwrite.getParts().get(1)), false);
				return new HistoryElement(this)
						.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
						.setOptionalOne(target);
			default:
				Log.warn("Invalid mode (" + this.mode + ") for " + this);
				break;
			}
			return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
		} else {
			Log.error("Mismatching UserInputOverwrite (" + overwrite + ") for " + this);
			return new HistoryElement(this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof EssenceSelector) {
			switch (this.status) {
			case STATE_INPUT:
				this.getGameClient().unsetSelector(sel);
				this.storedInput = ((EssenceSelector) sel).getSelection();
				if (this.processOutput()) {
					this.makeIdle();
				}
				break;
			case STATE_OUTPUT:
				this.getGameClient().unsetSelector(sel);
				this.storedOutput = ((EssenceSelector) sel).getSelection();
				switch (this.mode) {
				case MODE_PLAYER:
				case MODE_SELF:
					this.makeIdle();
					return;
				case MODE_IN_PLAY:
					ArrayList<Tappable> targets = this.getPlayer().getTappablesInPlay();
					int i = 0;
					while (i < targets.size()) {
						if (targets.get(i) instanceof MagicItem || targets.get(i) instanceof Scroll) {
							targets.remove(i);
						} else {
							i++;
						}
					}
					this.getGameClient()
							.addSelector(new ImageSelector<Tappable>(this, targets, "Select card to place essence on"));
					this.status = STATE_TAPPABLE;
					return;
				default:
					Log.warn("Invalid mode (" + this.mode + ") for " + this);
					this.makeIdle();
					return;
				}
			default:
				Log.warn("Invalid state (" + this.status + ") for " + this);
				break;
			}
		} else if (sel instanceof ImageSelector) {
			if (this.status == STATE_TAPPABLE) {
				this.getGameClient().unsetSelector(sel);
				this.result = ((ImageSelector<Tappable>) sel).getResult();
				this.makeIdle();
			}
		}
	}

	private void makeIdle() {
		this.status = STATE_IDLE;
		switch (this.mode) {
		case MODE_PLAYER:
			this.getGame().abilityFinished(this,
					new UserInputOverwrite(this, this.storedInput, this.storedOutput, this.getPlayer()));
			break;
		case MODE_SELF:
			this.getGame().abilityFinished(this,
					new UserInputOverwrite(this, this.storedInput, this.storedOutput, this.getTappable()));
			break;
		case MODE_IN_PLAY:
			this.getGame().abilityFinished(this,
					new UserInputOverwrite(this, this.storedInput, this.storedOutput, this.result));
			break;
		default:
			Log.error("Invalid mode (" + this.mode + ") for " + this);
			break;
		}
		this.storedInput = null;
		this.storedOutput = null;
		this.result = null;
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}

}