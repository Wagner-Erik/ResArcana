package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.abilities.specials.Illusion;
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

public class CreatureTapper extends Ability implements Selecting {

	public static final int TAP_MODE_DRAGON = 0;
	public static final int TAP_MODE_BEAST = 1;
	public static final int TAP_MODE_DEMON = 2;

	public static final int OUTPUT_MODE_SELF = 0;
	public static final int OUTPUT_MODE_PLAYER = 1;

	private static final int STATE_IDLE = 0;
	private static final int STATE_SELECT = 1;
	private static final int STATE_ILLUSION = 2;
	private static final int STATE_REWARD = 3;

	private final int tapMode, outputMode;
	private final EssenceSelection reward;
	private final boolean tapping;

	private Tappable result;

	private EssenceSelection illusionCost;

	private int status = STATE_IDLE;

	public CreatureTapper(Tappable parent, Vector relPos, int tapMode, boolean tapping, int outputMode,
			EssenceSelection reward) {
		super(parent, relPos);
		this.tapMode = tapMode;
		this.outputMode = outputMode;
		this.reward = reward;
		this.tapping = tapping;
	}

	private ArrayList<Tappable> getTargets() {
		ArrayList<Tappable> targets = new ArrayList<Tappable>();
		for (Tappable tappable : this.getPlayer().getTappablesInPlay()) {
			if (!tappable.isTapped()) {
				switch (this.tapMode) {
				case TAP_MODE_DRAGON:
					if (tappable.isDragon() || (tappable instanceof Illusion
							&& this.getPlayer().isPayable(new EssenceSelection(null, 2)))) {
						targets.add(tappable);
					}
					break;
				case TAP_MODE_BEAST:
					if (tappable.isBeast() || tappable instanceof Illusion) {
						targets.add(tappable);
					}
					break;
				case TAP_MODE_DEMON:
					if (tappable.isDemon() || tappable instanceof Illusion) {
						targets.add(tappable);
					}
					break;
				default:
					Log.warn("Unknwon mode " + this.tapMode + " for " + this);
					break;
				}
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
		ArrayList<Tappable> targets = this.getTargets();
		String message = "";
		switch (this.tapMode) {
		case TAP_MODE_DRAGON:
			message = "Select dragon to tap";
			break;
		case TAP_MODE_BEAST:
			message = "Select beast to tap";
			break;
		case TAP_MODE_DEMON:
			message = "Select demon to tap";
			break;
		default:
			Log.warn("Unknwon mode " + this.tapMode + " for " + this);
			break;
		}
		this.getGameClient().addSelector(new ImageSelector<Tappable>(this, targets, message));
		this.status = STATE_SELECT;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		Tappable target = this.getGame().getTappable(overwrite.getParts().get(0));
		target.tap();
		this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(1)), true); // cost for illusion
		if (this.tapping) {
			this.getTappable().tap();
		}
		switch (this.outputMode) { // reward
		case OUTPUT_MODE_SELF:
			this.getTappable().modifyEssence(new EssenceSelection(overwrite.getParts().get(2)), false);
			break;
		case OUTPUT_MODE_PLAYER:
			this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(2)), false);
		default:
			break;
		}
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
				.setOptionalOne(target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_SELECT) {
			this.getGameClient().unsetSelector(sel);
			this.result = ((ImageSelector<Tappable>) sel).getResult();
			switch (this.tapMode) {
			case TAP_MODE_DRAGON:
				if (this.result instanceof Illusion) {
					this.getGameClient()
							.addSelector(new EssenceSelector(this, new EssenceSelection(null, 2),
									this.getPlayer().getEssenceCounter().getCount(),
									"Pay essences to tap Illusion as a dragon"));
					this.status = STATE_ILLUSION;
					break;
				}
			case TAP_MODE_BEAST:
			case TAP_MODE_DEMON:
				this.illusionCost = new EssenceSelection();
				this.status = STATE_IDLE;
				if (this.reward.isDetermined()) {
					this.getGame().abilityFinished(this,
							new UserInputOverwrite(this, this.result, this.illusionCost, this.reward));
				} else {
					this.getGameClient().addSelector(new EssenceSelector(this, this.reward,
							"Choose output for " + this.getTappable().getName()));
					this.status = STATE_REWARD;
				}
				break;
			default:
				Log.warn("Unknwon mode " + this.tapMode + " for " + this);
				break;
			}
		} else if (sel instanceof EssenceSelector && this.status == STATE_ILLUSION) {
			this.getGameClient().unsetSelector(sel);
			this.illusionCost = ((EssenceSelector) sel).getSelection();
			this.status = STATE_IDLE;
			if (this.reward.isDetermined()) {
				this.getGame().abilityFinished(this,
						new UserInputOverwrite(this, this.result, this.illusionCost, this.reward));
			} else {
				this.getGameClient().addSelector(
						new EssenceSelector(this, this.reward, "Choose output for " + this.getTappable().getName()));
				this.status = STATE_REWARD;
			}
		} else if (sel instanceof EssenceSelector && this.status == STATE_REWARD) {
			this.getGameClient().unsetSelector(sel);
			this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.result, this.illusionCost,
					((EssenceSelector) sel).getSelection()));
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
